CREATE OR REPLACE package get_data_pkg as 
  procedure testJson;
  function get_data(aSql in clob, aArgInfo in varchar2_200_table, aArgNumberList in number_table, aArgCharList in varchar2_200_table) return sys_refcursor;
end get_data_pkg;
/


CREATE OR REPLACE package body get_data_pkg as 
  TYPE arg_rec is RECORD (
    valueStartIndex number,
    valueCount number,
    valueType varchar2(20),
    argName varchar2(20)
    );
  type arg_table is table of arg_rec;
  
  TYPE bind_rec is RECORD (
    valueType varchar2(20),
    varcharVal varchar2(200),
    varcharTableVal varchar2_200_table,
    numberVal varchar2(200),
    numberTableVal number_table
    );
  type bind_table is table of bind_rec;

  procedure testJson is
    tmpClob clob := '[ {
      "name" : "x",
      "type" : "String",
      "value" : "as"
    }, {
      "name" : "y",
      "type" : "NumberTable",
      "value" : [ 0, 1, 2, 3, 4]
    } ]';
    tmpParamList json_array_t;
    tmpParam json_object_t;
    tmpProps json_key_list; 
    tmpProp json_object_t; 
    tmpValueList json_array_t;
    
    tmpName varchar2(50);
    tmpType varchar2(50);
  begin
    tmpParamList := JSON_ARRAY_T(tmpClob);
    for i in 0..tmpParamList.get_size-1 loop
     tmpParam := TREAT (tmpParamList.get(i) AS json_object_t);
     tmpName := tmpParam.get_string('name');
     tmpType := tmpParam.get_string('type');
          
     if tmpName = 'x' then
       dbms_output.put_line('-->Value: ' || tmpParam.get_string('value'));
     elsif tmpType = 'y' then
       tmpValueList := tmpParam.get_array('value'); -- TREAT(l_dept_obj.get(l_dept_key_list(j)) AS JSON_ARRAY_T);
       dbms_output.put_line('-->Value: ' || tmpValueList.get(1).to_number);
     end if;
    end loop;
    
  end testJson;
  
  procedure get_params_from_json(aJsonArgs in clob, aLastNamePart out varchar2, aEmpIds out number_table) is
    tmpParamList json_array_t;
    tmpParam json_object_t;
    tmpName varchar2(50);
    tmpValueList json_array_t;
  begin
    aLastNamePart := null;
    aEmpIds := number_table();
    
    tmpParamList := JSON_ARRAY_T(aJsonArgs);
    for i in 0..tmpParamList.get_size-1 loop
      tmpParam := TREAT (tmpParamList.get(i) AS json_object_t);
      tmpName := tmpParam.get_string('name');
          
      if tmpName = 'lastNamePart' then
        aLastNamePart := tmpParam.get_string('value');
      elsif tmpName = 'empIds' then
        tmpValueList := tmpParam.get_array('value');
        aEmpIds.extend(tmpValueList.get_size);
        for j in 0..tmpValueList.get_size-1 loop
          aEmpIds(j+1) := tmpValueList.get(j).to_number;
        end loop;
      end if;
    end loop;
  end get_params_from_json;
  
  procedure get_params_from_table(aArgs in varchar2_200_table, aLastNamePart out varchar2, aEmpIds out number_table) is
    tmpItem varchar2(200);
    tmpArgCounter number;
    tmpArgType varchar2(2);
    tmpArgValue varchar2(200);
    tmpPrevArgCounter number;
    tmpMinusPos number;
    tmpColonPos number;
  begin
    aLastNamePart := null;
    aEmpIds := number_table();

    tmpPrevArgCounter := 0;
    for i in 1..aArgs.count loop
      tmpItem := aArgs(i);
      
      tmpMinusPos := instr(tmpItem, '-', 1);
      tmpColonPos := instr(tmpItem, ':', tmpMinusPos);
      
      tmpArgCounter := to_number(substr(tmpItem, 1, tmpMinusPos - 1));
      tmpArgType := substr(tmpItem, tmpMinusPos + 1, tmpColonPos - tmpMinusPos - 1);
      tmpArgValue := substr(tmpItem, tmpColonPos + 1);
      
      if tmpArgCounter = 1 then
        aLastNamePart := tmpArgValue;
      else
        aEmpIds.extend;
        aEmpIds(aEmpIds.count) := to_number(tmpArgValue);
      end if;
    end loop;
  end get_params_from_table;
    
    
    
    
    
  function get_arg_table(aArgInfo in varchar2_200_table) return arg_table is
    tmpArgTable arg_table := arg_table();
    tmpArgRec arg_rec;
    tmpEmptyArgRec arg_rec;
    tmpArg varchar2(200);
    tmpToken varchar2(200);
    tmpToken2StartPos number;
    tmpToken3StartPos number;
    tmpToken4StartPos number;
  begin
    for i in 1..aArgInfo.count loop
      tmpArg := aArgInfo(i);
      tmpArgRec := tmpEmptyArgRec;
      
      tmpToken2StartPos := instr(tmpArg, '-', 1) + 1;
      tmpToken3StartPos := instr(tmpArg, '-', tmpToken2StartPos + 1) + 1;
      tmpToken4StartPos := instr(tmpArg, '-', tmpToken3StartPos + 1) + 1;
      
      tmpToken := substr(tmpArg, 1, tmpToken2StartPos - 2);
      tmpArgRec.valueStartIndex := to_number(tmpToken);
      
      tmpToken := substr(tmpArg, tmpToken2StartPos, tmpToken3StartPos - tmpToken2StartPos - 1);
      tmpArgRec.valueCount := to_number(tmpToken);
      
      tmpToken := substr(tmpArg, tmpToken3StartPos, tmpToken4StartPos - tmpToken3StartPos - 1);
      tmpArgRec.valueType := tmpToken;
      
      tmpToken := substr(tmpArg, tmpToken4StartPos);
      tmpArgRec.argName := tmpToken;
      
      tmpArgTable.extend;
      tmpArgTable(tmpArgTable.count) := tmpArgRec;
    end loop;
    
    return tmpArgTable;
    exception when others then
      raise_application_error(-20001,  dbms_utility.format_error_backtrace || 
        'Error persing: ' || tmpArg || '-' || tmpToken || '(' || tmpToken2StartPos || ',' || tmpToken3StartPos || ',' || tmpToken4StartPos || '): ' || sqlerrm);
  end get_arg_table;
  
  function get_bind_table(aArgInfo in varchar2_200_table, aArgNumberList in number_table, aArgCharList in varchar2_200_table) return bind_table is
    tmpBindTable bind_table := bind_table();
    tmpBindRec bind_rec;
    tmpEmptyBindRec bind_rec;
    tmpArgTable arg_table;
    tmpArgRec arg_rec;
    tmpCharTable varchar2_200_table;
    tmpNumberTable number_table;
  begin 
    tmpArgTable := get_arg_table(aArgInfo);
    for i in 1..tmpArgTable.count loop
      tmpArgRec := tmpArgTable(i);
      
      tmpBindRec := tmpEmptyBindRec;
      tmpBindRec.valueType := tmpArgRec.valueType;
      
      if tmpBindRec.valueType = 'string' then
        tmpBindRec.varcharVal := aArgCharList(tmpArgRec.valueStartIndex);
      elsif tmpBindRec.valueType = 'number' then
        tmpBindRec.numberVal := aArgNumberList(tmpArgRec.valueStartIndex);
      elsif tmpBindRec.valueType = 'string_table' then
        tmpCharTable := varchar2_200_table();
        for j in 1..tmpArgRec.valueCount loop
          tmpCharTable.extend;
          tmpCharTable(tmpCharTable.count) := aArgCharList(tmpArgRec.valueStartIndex + j - 1);
        end loop;
        tmpBindRec.varcharTableVal := tmpCharTable;
      elsif tmpBindRec.valueType = 'number_table' then
        tmpNumberTable := number_table();
        for j in 1..tmpArgRec.valueCount loop
          tmpNumberTable.extend;
          tmpNumberTable(tmpNumberTable.count) := aArgNumberList(tmpArgRec.valueStartIndex + j - 1);
        end loop;
        tmpBindRec.numberTableVal := tmpNumberTable;
      else
        raise_application_error(-20001, 'Wrong value type given: ' || tmpBindRec.valueType);
      end if;
      
      tmpBindTable.extend;
      tmpBindTable(tmpBindTable.count) := tmpBindRec;
    end loop;
    return tmpBindTable;
  end get_bind_table;
  
  function getCur(aSql in clob, aBindTable in bind_table) return sys_refcursor is
    tmpBindRec bind_rec;
    tmpCurNum number;  
    tmpDummy pls_integer;  
    tmpCur sys_refcursor;  
  begin
    tmpCurNum := DBMS_SQL.open_cursor;  
    DBMS_SQL.parse(tmpCurNum, aSql, DBMS_SQL.native);  
  
    for i in 1..aBindTable.count loop
      tmpBindRec := aBindTable(i);
      
      if tmpBindRec.valueType = 'string' then
        DBMS_SQL.bind_variable(tmpCurNum, 'p' || to_char(i), tmpBindRec.varcharVal);
      elsif tmpBindRec.valueType = 'number' then
        DBMS_SQL.bind_variable(tmpCurNum, 'p' || to_char(i), tmpBindRec.numberVal);
      elsif tmpBindRec.valueType = 'string_table' then
        DBMS_SQL.bind_variable(tmpCurNum, 'p' || to_char(i), tmpBindRec.varcharTableVal);
      elsif tmpBindRec.valueType = 'number_table' then
        DBMS_SQL.bind_variable(tmpCurNum, 'p' || to_char(i), tmpBindRec.numberTableVal);
      else
        raise_application_error(-20001, 'Wrong value type given: ' || tmpBindRec.valueType);
      end if;

    end loop;
  
    tmpDummy := DBMS_SQL.EXECUTE(tmpCurNum);  
    tmpCur := DBMS_SQL.to_refcursor(tmpCurNum);  
    return tmpCur;  
  end getCur;

  function get_data(aSql in clob, aArgInfo in varchar2_200_table, aArgNumberList in number_table, aArgCharList in varchar2_200_table) return sys_refcursor is
    PRAGMA AUTONOMOUS_TRANSACTION;
    tmpCur sys_refcursor;
    tmpLastNamePart varchar2(200);
    tmpEmpIdTable number_table;
    tmpLogArgs clob;
    tmpTimeStart number := dbms_utility.get_time;
    tmpSql clob;
    tmpTimeDiff number;
    tmpBindTable bind_table;
  begin
    tmpBindTable := get_bind_table(aArgInfo, aArgNumberList, aArgCharList);
    tmpTimeDiff := (dbms_utility.get_time - tmpTimeStart) / 100;
    
    SELECT 'Time2Parsde[s]:' || tmpTimeDiff || LISTAGG(column_value, ',') WITHIN GROUP (order by null) into tmpLogArgs from table(aArgInfo);
    insert into sql_log(log_date, sql, args) values (sysdate, aSql, tmpLogArgs);
    commit;
    
    tmpCur := getCur(aSql, tmpBindTable);
    return tmpCur;
  end get_data;

end get_data_pkg;
/
