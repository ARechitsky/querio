package test
import java.io.File
import javax.sql.DataSource

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import querio.codegen.DatabaseGenerator
import querio.json.JSON4SExtension
import querio.vendor.PostgreSQL

import scalax.file.Path

object SourcesGenerator extends SQLUtil {
  def main(args: Array[String]) {
    val dir = Path(new File(args(0)))
    println(s"Dir: $dir")
    val pg: EmbeddedPostgres = EmbeddedPostgres.start()
    val dataSource: DataSource = pg.getPostgresDatabase()
    inStatement(dataSource) {stmt =>
      stmt.executeUpdate(BaseScheme.sql)
    }
    inConnection(dataSource) {connection =>
      new DatabaseGenerator(new PostgreSQL with JSON4SExtension, connection, null,
        pkg = "model.db.table",
        tableListClass = "model.db.Tables",
        dir = dir,
        isDefaultDatabase = true).generateDb()
    }
  }
}