ALTER TABLE /*# tableName */
        /*%for column : columns */
        ADD COLUMN IF NOT EXISTS /*# column.name */ /*# column.type */
          /*%if column_has_next */
          ,
          /*%end */
        /*%end */
        DROP COLUMN /*# column2.name */ /*# column2.type */
