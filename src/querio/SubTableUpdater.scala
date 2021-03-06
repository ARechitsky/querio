package querio
import scala.collection.mutable

/**
 * Автоматический обновлятор записей подтаблиц.
 * Его предназначение - поддерживать ряд записей подчинённой таблицы, чтобы они соответствовали заданному списку значений.
 * Обновлятор работает бережливо. Он не будет удалять записи, когда можно просто обновить данные в них.
 *
 * Обновлятор создаётся методом Table.createSubTableUpdater, и хранится, как правило, в самом объекте Table.
 */
class SubTableUpdater[TR <: TableRecord, MTR <: MutableTableRecord[TR], V]
(table: Table[TR, MTR], get: TR => V, create: (MTR, Int) => Any, update: (MTR, V) => Any)(implicit db: DbTrait) {

  /**
   * Обновить значения подтаблиц по заданному списку newValues.
   */
  def update(parentId: Int, newValues: Set[V], maybeSubTableList: Option[SubTableList[TR, MTR]])(implicit dt: DataTr) {
    maybeSubTableList match {
      case Some(subTableList) =>
        val remainValues: mutable.Set[V] = newValues.to[mutable.Set]
        val obsoleteRecords = mutable.Buffer[TR]()

        // Выяснить, какие записи можно не трогать, какие новые, а какие старые
        for (record <- subTableList.items) {
          val value: V = get(record)
          if (remainValues.contains(value)) remainValues -= value
          else obsoleteRecords += record
        }
        // Поменять значения оставшимся записям
        while (remainValues.nonEmpty && obsoleteRecords.nonEmpty) {
          val value = remainValues.head
          val record = obsoleteRecords.head
          val mutableRecord: MTR = record.toMutable.asInstanceOf[MTR]
          update(mutableRecord, value)
          db.updateChanged(record, mutableRecord)
          remainValues -= value
          obsoleteRecords.remove(0)
        }
        // Удалить лишние значения
        obsoleteRecords.foreach(r => db.delete(table, r._primaryKey))
        // Добавить новые
        addNew(parentId, remainValues)

      case None =>
        addNew(parentId, newValues)
    }
  }

  /**
   * Добавить новые значения
   */
  protected def addNew(parentId: Int, values: Iterable[V])(implicit dt: DataTr) {
    for (value <- values) {
      val mutableRecord = table._newMutableRecord
      create(mutableRecord, parentId)
      update(mutableRecord, value)
      db.insert(mutableRecord)
    }
  }
}
