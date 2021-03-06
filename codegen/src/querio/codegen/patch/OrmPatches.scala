package querio.codegen.patch
import java.io.File

import querio.vendor.Vendor

import scalax.file.Path

class OrmPatches(val vendor: Vendor) {
  val currentVersion = 2

  private def patch(lines: List[String], fromVersion: Int): List[String] = (fromVersion match {
    case 0 => OrmPatch0
    case 1 => OrmPatch1
  }).patch(lines)

  // ------------------------------- Inner methods -------------------------------

  val versionR = """// (?:querio|orm)Version: (\d)+\s*""".r

  def autoPatchChopVersion(original: List[String]): List[String] = {
    val (versionLines, lines) = original.partition(versionR.pattern.matcher(_).matches())
    var version: Int = versionLines match {
      case List(line) => versionR.findFirstMatchIn(line).get.group(1).toInt
      case Nil => 0
    }
    var patched = lines
    require(version <= currentVersion, "Invalid version: " + version)
    while (version < currentVersion) {
      patched = patch(patched, version)
      version += 1
    }
//    saveToTemp(patched)
    patched
  }

  private def saveToTemp(lines: List[String]): Unit = Path(new File("/tmp/tt.scala")).write(lines.mkString("\n"))
}
