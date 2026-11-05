#!/usr/bin/env python3
"""Prepare Spigot/Paper NMS artifacts for BuildingGame CI builds.

The official `org.spigotmc:spigot:*:remapped-mojang` artifacts are not published
anywhere, so we rebuild them ourselves:
  1. download `remapped-obf` jar + `maps-mojang` mapping from the CodeMC mirror
  2. remap obf -> mojang with SpecialSource
  3. install into the local Maven repository under the coordinates the build expects

Covers:
  - org.spigotmc:spigot:<ver>:jar:remapped-mojang  (1.20.5 ~ 1.21.11)
  - org.spigotmc:spigot:<ver>                      (26.2, main jar is already mojang-mapped)
  - ca.bkaw:paper-nms:<ver>-SNAPSHOT               (1.20.1 ~ 1.20.4, spigot-remapped substitute)

Run:  python3 ci/seed_spigot.py
Env:  MVN_REPO  extra `-Dmaven.repo.local` value (optional)
      JAVA_HOME java to run SpecialSource (defaults to `java` on PATH)
"""

import os
import shutil
import re
import struct
import subprocess
import sys
import urllib.request

BASE = "https://repo.codemc.io/repository/nms/org/spigotmc"
CENTRAL = "https://repo.maven.apache.org/maven2"
SPECIAL_SOURCE_URL = CENTRAL + "/net/md-5/SpecialSource/1.11.6/SpecialSource-1.11.6-shaded.jar"

# shaded jar 内嵌的 ASM 过旧（不支持 Java 21+ 的 major 65+），用新版 ASM 前置覆盖
ASM_OVERRIDE = [
    CENTRAL + "/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar",
    CENTRAL + "/org/ow2/asm/asm-commons/9.7.1/asm-commons-9.7.1.jar",
    CENTRAL + "/org/ow2/asm/asm-tree/9.7.1/asm-tree-9.7.1.jar",
    CENTRAL + "/org/ow2/asm/asm-util/9.7.1/asm-util-9.7.1.jar",
    CENTRAL + "/org/ow2/asm/asm-analysis/9.7.1/asm-analysis-9.7.1.jar",
]

SPIGOT_VERSIONS = [
    "1.20.5-R0.1-SNAPSHOT",
    "1.20.6-R0.1-SNAPSHOT",
    "1.21.1-R0.1-SNAPSHOT",
    "1.21.3-R0.1-SNAPSHOT",
    "1.21.4-R0.1-SNAPSHOT",
    "1.21.5-R0.1-SNAPSHOT",
    "1.21.8-R0.1-SNAPSHOT",
    "1.21.10-R0.1-SNAPSHOT",
    "1.21.11-R0.2-SNAPSHOT",
]
PLAIN_MOJANG_VERSIONS = ["26.2-R0.1-SNAPSHOT"]
PAPER_VERSIONS = ["1.20.1-R0.1-SNAPSHOT", "1.20.2-R0.1-SNAPSHOT", "1.20.3-R0.1-SNAPSHOT", "1.20.4-R0.1-SNAPSHOT"]

# third-party libs referenced by NMS classes during compilation
EXTRA_DEPS = [
    "org.jspecify:jspecify:1.0.0",
    "it.unimi.dsi:fastutil:8.5.13",
    "com.google.guava:guava:33.3.1-jre",
    "com.google.code.gson:gson:2.11.0",
    "org.yaml:snakeyaml:2.2",
    "org.apache.commons:commons-lang3:3.14.0",
    "org.joml:joml:1.10.8",
    "com.mojang:brigadier:1.3.10",
    "com.mojang:datafixerupper:9.0.19",
    "com.mojang:authlib:7.0.61",
]

MVN_REPO = os.environ.get("MVN_REPO", "")
JAVA = os.environ.get("JAVA_HOME", "")


def log(msg):
    print(msg, flush=True)


def get(url, binary=False, tries=3):
    last = None
    for _ in range(tries):
        try:
            data = urllib.request.urlopen(url, timeout=300).read()
            return data if binary else data.decode("utf-8", "replace")
        except Exception as e:
            last = e
    raise last


def snap_value(xml, classifier):
    for m in re.finditer(r"<snapshotVersion>(.*?)</snapshotVersion>", xml, re.S):
        blk = m.group(1)
        if "<classifier>" + classifier + "</classifier>" in blk:
            v = re.search(r"<value>([^<]+)</value>", blk)
            if v:
                return v.group(1)
    return None


def plain_or_pom_value(xml, ext):
    for m in re.finditer(r"<snapshotVersion>(.*?)</snapshotVersion>", xml, re.S):
        blk = m.group(1)
        if "<extension>" + ext + "</extension>" in blk and "<classifier>" not in blk:
            v = re.search(r"<value>([^<]+)</value>", blk)
            if v:
                return v.group(1)
    return None


def synth_pom(groupId, artifactId, version, api_version=None):
    deps = []
    if api_version:
        deps.append(
            "    <dependency><groupId>org.spigotmc</groupId><artifactId>spigot-api</artifactId>"
            "<version>%s</version></dependency>" % api_version
        )
    for d in EXTRA_DEPS:
        g, a, v = d.split(":")
        deps.append("    <dependency><groupId>%s</groupId><artifactId>%s</artifactId><version>%s</version></dependency>" % (g, a, v))
    return (
        '<project xmlns="http://maven.apache.org/POM/4.0.0">\n'
        "  <modelVersion>4.0.0</modelVersion>\n"
        "  <groupId>%s</groupId>\n"
        "  <artifactId>%s</artifactId>\n"
        "  <version>%s</version>\n"
        "  <dependencies>\n%s\n  </dependencies>\n"
        "</project>\n" % (groupId, artifactId, version, "\n".join(deps))
    )


def mvn_install(file, pom, groupId, artifactId, version, classifier=None):
    cmd = ["mvn", "-B", "-q", "install:install-file", "-Dfile=" + file, "-DpomFile=" + pom,
           "-DgroupId=" + groupId, "-DartifactId=" + artifactId, "-Dversion=" + version, "-Dpackaging=jar"]
    if classifier:
        cmd.append("-Dclassifier=" + classifier)
    if MVN_REPO:
        cmd.append("-Dmaven.repo.local=" + MVN_REPO)
    if os.name == "nt":
        # Windows 下 mvn 是 .cmd，必须经 shell 启动
        exe = shutil.which("mvn") or "mvn"
        cmdline = subprocess.list2cmdline([exe] + cmd[1:])
        r = subprocess.run(cmdline, shell=True, capture_output=True, text=True)
    else:
        r = subprocess.run(cmd, capture_output=True, text=True)
    if r.returncode != 0:
        log("  install FAILED:\n" + (r.stdout or "") + (r.stderr or ""))
        raise SystemExit(1)


def remap(java, ss_cp, in_jar, maps, out_jar):
    cmd = [java, "-Xmx2G", "-cp", ss_cp, "net.md_5.specialsource.SpecialSource",
           "-i", in_jar, "-o", out_jar, "-m", maps, "-q"]
    r = subprocess.run(cmd, capture_output=True, text=True)
    if r.returncode != 0:
        log("  remap FAILED:\n" + (r.stdout or "") + (r.stderr or ""))
        raise SystemExit(1)


def main():
    work = os.path.join(os.environ.get("RUNNER_TEMP", os.environ.get("TEMP", "/tmp")), "nms-seed")
    os.makedirs(work, exist_ok=True)

    java = os.path.join(JAVA, "bin", "java.exe") if JAVA and os.name == "nt" else \
           os.path.join(JAVA, "bin", "java") if JAVA else "java"

    ss_jar = os.path.join(work, "SpecialSource-shaded.jar")
    if not os.path.exists(ss_jar):
        log("下载 SpecialSource shaded jar")
        open(ss_jar, "wb").write(get(SPECIAL_SOURCE_URL, binary=True))
    asm_jars = []
    for url in ASM_OVERRIDE:
        p = os.path.join(work, os.path.basename(url))
        if not os.path.exists(p):
            open(p, "wb").write(get(url, binary=True))
        asm_jars.append(p)
    ss_cp = os.pathsep.join(asm_jars + [ss_jar])

    def download(url, path, binary=False):
        if os.path.exists(path) and os.path.getsize(path) > 1000:
            return
        log("  下载 " + os.path.basename(path))
        data = get(url, binary=binary)
        open(path, "wb").write(data if binary else data.encode("utf-8"))

    def process_spigot(ver, remap_to_mojang=True):
        tag = ver.replace("-SNAPSHOT", "")
        d = os.path.join(work, tag)
        os.makedirs(d, exist_ok=True)
        meta = get(BASE + "/spigot/" + ver + "/maven-metadata.xml")
        obf = snap_value(meta, "remapped-obf")
        ms = get(BASE + "/minecraft-server/" + ver + "/maven-metadata.xml")
        maps = snap_value(ms, "maps-mojang")
        if not obf or not maps:
            raise SystemExit("缺少制品: %s (obf=%s maps=%s)" % (ver, obf, maps))
        obf_p = os.path.join(d, "obf.jar")
        maps_p = os.path.join(d, "maps.txt")
        out_p = os.path.join(d, "mojang.jar")
        pom_p = os.path.join(d, "synth.pom")
        download(BASE + "/spigot/" + ver + "/spigot-" + obf + "-remapped-obf.jar", obf_p, binary=True)
        download(BASE + "/minecraft-server/" + ver + "/minecraft-server-" + maps + "-maps-mojang.txt", maps_p)
        log("[REMAP] " + ver)
        remap(java, ss_cp, obf_p, maps_p, out_p)
        open(pom_p, "w", encoding="utf-8").write(synth_pom("org.spigotmc", "spigot", ver, api_version=ver))
        mvn_install(out_p, pom_p, "org.spigotmc", "spigot", ver, classifier="remapped-mojang")
        # specialsource-maven-plugin 的 remap-spigot 执行需要 obf 分类器
        mvn_install(obf_p, pom_p, "org.spigotmc", "spigot", ver, classifier="remapped-obf")
        log("  OK " + ver)

    def process_plain_mojang(ver):
        """主 jar 本身即 mojang 映射（26.x），无需重映射，直接安装为无 classifier 制品。"""
        tag = ver.replace("-SNAPSHOT", "")
        d = os.path.join(work, tag)
        os.makedirs(d, exist_ok=True)
        meta = get(BASE + "/spigot/" + ver + "/maven-metadata.xml")
        jv = plain_or_pom_value(meta, "jar")
        pv = plain_or_pom_value(meta, "pom")
        jar_p = os.path.join(d, "plain.jar")
        pom_p = os.path.join(d, "synth.pom")
        download(BASE + "/spigot/" + ver + "/spigot-" + jv + ".jar", jar_p, binary=True)
        download(BASE + "/spigot/" + ver + "/spigot-" + pv + ".pom", pom_p)
        log("[INSTALL] " + ver + " (主 jar 即 mojang 映射)")
        mvn_install(jar_p, pom_p, "org.spigotmc", "spigot", ver)
        log("  OK " + ver)

    def process_paper(ver):
        """1.20.1~1.20.4：Paper 只发布 paperclip（二进制补丁，无法离线还原），
        改用同版本 Spigot 重映射后以 ca.bkaw:paper-nms 坐标安装。"""
        short = ver.replace("-R0.1-SNAPSHOT", "")
        d = os.path.join(work, "paper-" + short)
        os.makedirs(d, exist_ok=True)
        meta = get(BASE + "/spigot/" + ver + "/maven-metadata.xml")
        obf = snap_value(meta, "remapped-obf")
        ms = get(BASE + "/minecraft-server/" + ver + "/maven-metadata.xml")
        maps = snap_value(ms, "maps-mojang")
        if not obf or not maps:
            raise SystemExit("缺少制品: %s" % ver)
        obf_p = os.path.join(d, "obf.jar")
        maps_p = os.path.join(d, "maps.txt")
        out_p = os.path.join(d, "mojang.jar")
        pom_p = os.path.join(d, "synth.pom")
        download(BASE + "/spigot/" + ver + "/spigot-" + obf + "-remapped-obf.jar", obf_p, binary=True)
        download(BASE + "/minecraft-server/" + ver + "/minecraft-server-" + maps + "-maps-mojang.txt", maps_p)
        log("[REMAP] paper-nms " + short)
        remap(java, ss_cp, obf_p, maps_p, out_p)
        open(pom_p, "w", encoding="utf-8").write(synth_pom("ca.bkaw", "paper-nms", short + "-SNAPSHOT", api_version=ver))
        mvn_install(out_p, pom_p, "ca.bkaw", "paper-nms", short + "-SNAPSHOT")
        log("  OK paper-nms " + short)

    for v in SPIGOT_VERSIONS:
        process_spigot(v)
    for v in PLAIN_MOJANG_VERSIONS:
        process_plain_mojang(v)
    for v in PAPER_VERSIONS:
        process_paper(v)

    log("\n全部 NMS 制品就绪")


if __name__ == "__main__":
    main()
