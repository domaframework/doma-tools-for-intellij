package doma.example.dao.inspection.paramtype;

import doma.example.entity.*;
import org.seasar.doma.Dao;
import org.seasar.doma.Delete;
import org.seasar.doma.Insert;
import org.seasar.doma.Update;
import org.seasar.doma.Sql;

@Dao
public interface InsertUpdateDeleteParamTestDao {

    @Insert
    int insertEntity(Packet packet);

    @Insert
    int insertPrimitive(Integer <error descr="The parameter type must be an entity class">id</error>);

    @Update
    int <error descr="The number of parameters must be one">updateNoParams</error>();

    @Delete
    int <error descr="The number of parameters must be one">deleteMultipleParams</error>(Packet packet, String name);

    @Insert
    @Sql("insert into Packet (id, name) values (/* id */0, /* name */'test')")
    int insertWithSqlAnnotation(Integer id, String name);

}
