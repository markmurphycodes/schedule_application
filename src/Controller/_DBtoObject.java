package Controller;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface _DBtoObject<T>
{
    public T createFromDB(ResultSet _rs) throws SQLException;
}
