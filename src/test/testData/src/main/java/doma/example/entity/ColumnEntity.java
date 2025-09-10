package doma.example.entity;

import java.util.ArrayList;
import java.util.List;
import org.seasar.doma.Entity;
import org.seasar.doma.Table;
import org.seasar.doma.Column;
import org.seasar.doma.jdbc.type.LocalDateType;

@Entity
@Table(name = "column_entity")
public class ColumnEntity {
    @Column(name = "name")
    public String name;
    @Column(name = "alias")
    public String alias;

    public Integer currentYear(){

        return 2000;
    };

    public List<ColumnEntity> params(){
        return new ArrayList<>();
    }

    public String getAliasDiv(int index){
        return alias.indent(index);
    }
}

