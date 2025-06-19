package doma.example.dao.inspection.paramtype;

import org.seasar.doma.*;

@Dao
public interface ScriptParamTestDao {

    @Script
    void <error descr="The number of parameters must be zero">scriptString</error>(String script);

    @Script
    void <error descr="The number of parameters must be zero">scriptInt</error>(int value);
}

