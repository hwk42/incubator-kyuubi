/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kyuubi.engine.flink.operation

import java.sql.DatabaseMetaData

import org.apache.flink.table.api.EnvironmentSettings.DEFAULT_BUILTIN_CATALOG
import org.apache.flink.table.api.EnvironmentSettings.DEFAULT_BUILTIN_DATABASE
import org.apache.flink.table.types.logical.LogicalTypeRoot
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.SpanSugar._

import org.apache.kyuubi.engine.flink.WithFlinkSQLEngine
import org.apache.kyuubi.engine.flink.result.Constants
import org.apache.kyuubi.operation.HiveJDBCTestHelper
import org.apache.kyuubi.operation.meta.ResultSetSchemaConstant._
import org.apache.kyuubi.service.ServiceState._

class FlinkOperationSuite extends WithFlinkSQLEngine with HiveJDBCTestHelper {
  override def withKyuubiConf: Map[String, String] = Map()

  override protected def jdbcUrl: String =
    s"jdbc:hive2://${engine.frontendServices.head.connectionUrl}/"

  ignore("release session if shared level is CONNECTION") {
    logger.info(s"jdbc url is $jdbcUrl")
    assert(engine.getServiceState == STARTED)
    withJdbcStatement() { _ => }
    eventually(Timeout(20.seconds)) {
      assert(engine.getServiceState == STOPPED)
    }
  }

  test("get catalogs") {
    withJdbcStatement() { statement =>
      val meta = statement.getConnection.getMetaData
      val catalogs = meta.getCatalogs
      val expected = Set("default_catalog").toIterator
      while (catalogs.next()) {
        assert(catalogs.getString(TABLE_CAT) === expected.next())
      }
      assert(!expected.hasNext)
      assert(!catalogs.next())
    }
  }

  test("get type info") {
    withJdbcStatement() { statement =>
      val typeInfo = statement.getConnection.getMetaData.getTypeInfo

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.CHAR.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.CHAR)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.VARCHAR.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.VARCHAR)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.BOOLEAN.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.BOOLEAN)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.BINARY.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.BINARY)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.VARBINARY.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.VARBINARY)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.DECIMAL.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.DECIMAL)
      assert(typeInfo.getInt(PRECISION) === 38)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 10)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.TINYINT.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.TINYINT)
      assert(typeInfo.getInt(PRECISION) === 3)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 10)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.SMALLINT.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.SMALLINT)
      assert(typeInfo.getInt(PRECISION) === 5)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 10)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.INTEGER.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.INTEGER)
      assert(typeInfo.getInt(PRECISION) === 10)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 10)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.BIGINT.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.BIGINT)
      assert(typeInfo.getInt(PRECISION) === 19)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 10)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.FLOAT.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.FLOAT)
      assert(typeInfo.getInt(PRECISION) === 7)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 10)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.DOUBLE.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.DOUBLE)
      assert(typeInfo.getInt(PRECISION) === 15)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 10)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.DATE.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.DATE)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.TIME_WITHOUT_TIME_ZONE.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.TIME)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.TIMESTAMP_WITHOUT_TIME_ZONE.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.TIMESTAMP)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.TIMESTAMP_WITH_TIME_ZONE.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 0)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(
        typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.TIMESTAMP_WITH_LOCAL_TIME_ZONE.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.TIMESTAMP_WITH_TIMEZONE)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 0)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.INTERVAL_YEAR_MONTH.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.OTHER)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 0)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.INTERVAL_DAY_TIME.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.OTHER)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 0)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.ARRAY.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.ARRAY)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 0)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.MULTISET.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.JAVA_OBJECT)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 0)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.MAP.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.JAVA_OBJECT)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 0)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.ROW.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.JAVA_OBJECT)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 0)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.DISTINCT_TYPE.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.OTHER)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 0)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.STRUCTURED_TYPE.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.OTHER)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 0)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.NULL.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.NULL)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 3)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.RAW.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.OTHER)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 0)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.SYMBOL.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.OTHER)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 0)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)

      typeInfo.next()
      assert(typeInfo.getString(TYPE_NAME) === LogicalTypeRoot.UNRESOLVED.name())
      assert(typeInfo.getInt(DATA_TYPE) === java.sql.Types.OTHER)
      assert(typeInfo.getInt(PRECISION) === 0)
      assert(typeInfo.getShort(NULLABLE) === 1)
      assert(!typeInfo.getBoolean(CASE_SENSITIVE))
      assert(typeInfo.getShort(SEARCHABLE) === 0)
      assert(typeInfo.getInt(NUM_PREC_RADIX) === 0)
    }
  }

  test("get schemas") {
    withJdbcStatement() { statement =>
      val metaData = statement.getConnection.getMetaData
      var resultSet = metaData.getSchemas(null, null)
      while (resultSet.next()) {
        assert(resultSet.getString(TABLE_SCHEM) === DEFAULT_BUILTIN_DATABASE)
        assert(resultSet.getString(TABLE_CATALOG) === DEFAULT_BUILTIN_CATALOG)
      }
      resultSet = metaData.getSchemas(
        DEFAULT_BUILTIN_CATALOG.split("_").apply(0),
        DEFAULT_BUILTIN_DATABASE.split("_").apply(0))
      while (resultSet.next()) {
        assert(resultSet.getString(TABLE_SCHEM) === DEFAULT_BUILTIN_DATABASE)
        assert(resultSet.getString(TABLE_CATALOG) === DEFAULT_BUILTIN_CATALOG)
      }
    }
  }

  test("get table types") {
    withJdbcStatement() { statement =>
      val meta = statement.getConnection.getMetaData
      val types = meta.getTableTypes
      val expected = Constants.SUPPORTED_TABLE_TYPES.toIterator
      while (types.next()) {
        assert(types.getString(TABLE_TYPE) === expected.next())
      }
      assert(!expected.hasNext)
      assert(!types.next())
    }
  }

  test("get tables") {
    val table = "table_1_test"
    val table_view = "table_1_test_view"

    withJdbcStatement(table) { statement =>
      statement.execute(
        s"""
           | create table $table (
           |  id int,
           |  name string,
           |  price double
           | ) with (
           |   'connector' = 'filesystem'
           | )
       """.stripMargin)

      statement.execute(
        s"""
           | create view ${table_view}
           | as select 1
       """.stripMargin)

      val metaData = statement.getConnection.getMetaData
      val rs1 = metaData.getTables(null, null, null, null)
      assert(rs1.next())
      assert(rs1.getString(1) == table)
      assert(rs1.next())
      assert(rs1.getString(1) == table_view)

      // get table , table name like table%
      val rs2 = metaData.getTables(null, null, "table%", Array("TABLE"))
      assert(rs2.next())
      assert(rs2.getString(1) == table)
      assert(!rs2.next())

      // get view , view name like *
      val rs3 = metaData.getTables(null, "default_database", "*", Array("VIEW"))
      assert(rs3.next())
      assert(rs3.getString(1) == table_view)

      // get view , view name like *, schema pattern like default_%
      val rs4 = metaData.getTables(null, "default_%", "*", Array("VIEW"))
      assert(rs4.next())
      assert(rs4.getString(1) == table_view)

      // get view , view name like *, schema pattern like no_exists_%
      val rs5 = metaData.getTables(null, "no_exists_%", "*", Array("VIEW"))
      assert(!rs5.next())
    }
  }

  test("get functions") {
    withJdbcStatement() { statement =>
      val metaData = statement.getConnection.getMetaData
      Seq("currentTimestamp", "currentDate", "currentTime", "localTimestamp", "localTime")
        .foreach { func =>
          val resultSet = metaData.getFunctions(null, null, func)
          while (resultSet.next()) {
            assert(resultSet.getString(FUNCTION_CAT) === null)
            assert(resultSet.getString(FUNCTION_SCHEM) === null)
            assert(resultSet.getString(FUNCTION_NAME) === func)
            assert(resultSet.getString(REMARKS) === null)
            assert(resultSet.getInt(FUNCTION_TYPE) === DatabaseMetaData.functionResultUnknown)
            assert(resultSet.getString(SPECIFIC_NAME) === null)
          }
        }
      val expected =
        List("currentTimestamp", "currentDate", "currentTime", "localTimestamp", "localTime")
      Seq("current", "local")
        .foreach { funcPattern =>
          val resultSet = metaData.getFunctions(null, null, funcPattern)
          while (resultSet.next()) {
            assert(resultSet.getString(FUNCTION_CAT) === null)
            assert(resultSet.getString(FUNCTION_SCHEM) === null)
            assert(expected.contains(resultSet.getString(FUNCTION_NAME)))
            assert(resultSet.getString(REMARKS) === null)
            assert(resultSet.getString(FUNCTION_TYPE) === DatabaseMetaData.functionResultUnknown)
            assert(resultSet.getString(SPECIFIC_NAME) === null)
          }
        }
    }
  }

  test("execute statement - select column name with dots") {
    withJdbcStatement() { statement =>
      val resultSet = statement.executeQuery("select 'tmp.hello'")
      assert(resultSet.next())
      assert(resultSet.getString(1) === "tmp.hello")
    }
  }

  test("execute statement - select decimal") {
    withJdbcStatement() { statement =>
      val resultSet = statement.executeQuery("SELECT 1.2BD, 1.23BD ")
      assert(resultSet.next())
      assert(resultSet.getBigDecimal(1) === java.math.BigDecimal.valueOf(1.2))
      assert(resultSet.getBigDecimal(2) === java.math.BigDecimal.valueOf(1.23))
      val metaData = resultSet.getMetaData
      assert(metaData.getColumnType(1) === java.sql.Types.DECIMAL)
      assert(metaData.getColumnType(2) === java.sql.Types.DECIMAL)
      assert(metaData.getPrecision(1) == 2)
      assert(metaData.getPrecision(2) == 3)
      assert(metaData.getScale(1) == 1)
      assert(metaData.getScale(2) == 2)
    }
  }

  test("execute statement - select varchar/char") {
    withJdbcStatement() { statement =>
      val resultSet =
        statement.executeQuery("select cast('varchar10' as varchar(10)), " +
          "cast('char16' as char(16))")
      val metaData = resultSet.getMetaData
      assert(metaData.getColumnType(1) === java.sql.Types.VARCHAR)
      assert(metaData.getPrecision(1) === 10)
      assert(metaData.getColumnType(2) === java.sql.Types.CHAR)
      assert(metaData.getPrecision(2) === 16)
      assert(resultSet.next())
      assert(resultSet.getString(1) === "varchar10")
      assert(resultSet.getString(2) === "char16          ")
    }
  }

  test("execute statement - select tinyint") {
    withJdbcStatement() { statement =>
      val resultSet = statement.executeQuery("select cast(1 as tinyint)")
      assert(resultSet.next())
      assert(resultSet.getByte(1) === 1)
      val metaData = resultSet.getMetaData
      assert(metaData.getColumnType(1) === java.sql.Types.TINYINT)
    }
  }

  test("execute statement - select smallint") {
    withJdbcStatement() { statement =>
      val resultSet = statement.executeQuery("select cast(1 as smallint)")
      assert(resultSet.next())
      assert(resultSet.getShort(1) === 1)
      val metaData = resultSet.getMetaData
      assert(metaData.getColumnType(1) === java.sql.Types.SMALLINT)
    }
  }

  test("execute statement - select int") {
    withJdbcStatement() { statement =>
      val resultSet = statement.executeQuery("select 1")
      assert(resultSet.next())
      assert(resultSet.getInt(1) === 1)
      val metaData = resultSet.getMetaData
      assert(metaData.getColumnType(1) === java.sql.Types.INTEGER)
    }
  }

  test("execute statement - select bigint") {
    withJdbcStatement() { statement =>
      val resultSet = statement.executeQuery("select cast(1 as bigint)")
      assert(resultSet.next())
      assert(resultSet.getLong(1) === 1)
      val metaData = resultSet.getMetaData
      assert(metaData.getColumnType(1) === java.sql.Types.BIGINT)
    }
  }

  test("execute statement - select date") {
    withJdbcStatement() { statement =>
      val resultSet = statement.executeQuery("select date '2022-01-01'")
      assert(resultSet.next())
      assert(resultSet.getDate(1).toLocalDate.toString == "2022-01-01")
      val metaData = resultSet.getMetaData
      assert(metaData.getColumnType(1) === java.sql.Types.DATE)
    }
  }

  test("execute statement - select timestamp") {
    withJdbcStatement() { statement =>
      val resultSet =
        statement.executeQuery(
          "select timestamp '2022-01-01 00:00:00', timestamp '2022-01-01 00:00:00.123456789'")
      val metaData = resultSet.getMetaData
      assert(metaData.getColumnType(1) === java.sql.Types.TIMESTAMP)
      assert(metaData.getColumnType(2) === java.sql.Types.TIMESTAMP)
      // 9 digits for fraction of seconds
      assert(metaData.getPrecision(1) == 29)
      assert(metaData.getPrecision(2) == 29)
      assert(resultSet.next())
      assert(resultSet.getTimestamp(1).toString == "2022-01-01 00:00:00.0")
      assert(resultSet.getTimestamp(2).toString == "2022-01-01 00:00:00.123456789")
    }
  }

  test("execute statement - select array") {
    withJdbcStatement() { statement =>
      val resultSet =
        statement.executeQuery("select array ['v1', 'v2', 'v3']")
      val metaData = resultSet.getMetaData
      assert(metaData.getColumnType(1) === java.sql.Types.ARRAY)
      assert(resultSet.next())
      assert(resultSet.getObject(1).toString == "[\"v1\",\"v2\",\"v3\"]")
    }
  }

  test("execute statement - select map") {
    withJdbcStatement() { statement =>
      val resultSet =
        statement.executeQuery("select map ['k1', 'v1', 'k2', 'v2']")
      assert(resultSet.next())
      assert(resultSet.getString(1) == "{k1=v1, k2=v2}")
      val metaData = resultSet.getMetaData
      assert(metaData.getColumnType(1) === java.sql.Types.JAVA_OBJECT)
    }
  }

  test("execute statement - select row") {
    withJdbcStatement() { statement =>
      val resultSet =
        statement.executeQuery("select (1, '2', true)")
      assert(resultSet.next())
      assert(
        resultSet.getString(1) == "{INT NOT NULL:1,CHAR(1) NOT NULL:\"2\",BOOLEAN NOT NULL:true}")
      val metaData = resultSet.getMetaData
      assert(metaData.getColumnType(1) === java.sql.Types.STRUCT)
    }
  }

  test("execute statement - select binary") {
    withJdbcStatement() { statement =>
      val resultSet = statement.executeQuery("select encode('kyuubi', 'UTF-8')")
      assert(resultSet.next())
      assert(
        resultSet.getString(1) == "kyuubi")
      val metaData = resultSet.getMetaData
      assert(metaData.getColumnType(1) === java.sql.Types.BINARY)
    }
  }

  test("execute statement - select float") {
    withJdbcStatement()({ statement =>
      val resultSet = statement.executeQuery("SELECT cast(0.1 as float)")
      assert(resultSet.next())
      assert(resultSet.getString(1) == "0.1")
      assert(resultSet.getFloat(1) == 0.1f)
    })
  }

  test("execute statement - show functions") {
    withJdbcStatement() { statement =>
      val resultSet = statement.executeQuery("show functions")
      val metadata = resultSet.getMetaData
      assert(metadata.getColumnName(1) == "function name")
      assert(resultSet.next())
    }
  }

  test("execute statement - show databases") {
    withJdbcStatement() { statement =>
      val resultSet = statement.executeQuery("show databases")
      val metadata = resultSet.getMetaData
      assert(metadata.getColumnName(1) == "database name")
      assert(resultSet.next())
      assert(resultSet.getString(1) == "default_database")
    }
  }

  test("execute statement - show tables") {
    withJdbcStatement() { statement =>
      val resultSet = statement.executeQuery("show tables")
      val metadata = resultSet.getMetaData
      assert(metadata.getColumnName(1) == "table name")
      assert(!resultSet.next())
    }
  }

  test("execute statement - explain query") {
    withJdbcStatement() { statement =>
      val resultSet = statement.executeQuery("explain select 1")
      val metadata = resultSet.getMetaData
      assert(metadata.getColumnName(1) == "result")
      assert(resultSet.next())
    }
  }

  test("execute statement - create/alter/drop catalog") {
    // TODO: validate table results after FLINK-25558 is resolved
    withJdbcStatement()({ statement =>
      statement.executeQuery("create catalog cat_a with ('type'='generic_in_memory')")
      assert(statement.execute("drop catalog cat_a"))
    })
  }

  test("execute statement - create/alter/drop database") {
    // TODO: validate table results after FLINK-25558 is resolved
    withJdbcStatement()({ statement =>
      statement.executeQuery("create database db_a")
      assert(statement.execute("alter database db_a set ('k1' = 'v1')"))
      assert(statement.execute("drop database db_a"))
    })
  }

  test("execute statement - create/alter/drop table") {
    // TODO: validate table results after FLINK-25558 is resolved
    withJdbcStatement()({ statement =>
      statement.executeQuery("create table tbl_a (a string)")
      assert(statement.execute("alter table tbl_a rename to tbl_b"))
      assert(statement.execute("drop table tbl_b"))
    })
  }

  test("execute statement - create/alter/drop view") {
    // TODO: validate table results after FLINK-25558 is resolved
    withMultipleConnectionJdbcStatement()({ statement =>
      statement.executeQuery("create view view_a as select 1")
      assert(statement.execute("alter view view_a rename to view_b"))
      assert(statement.execute("drop view view_b"))
    })
  }

  test("execute statement - insert into") {
    withMultipleConnectionJdbcStatement()({ statement =>
      statement.executeQuery("create table tbl_a (a int) with ('connector' = 'blackhole')")
      val resultSet = statement.executeQuery("insert into tbl_a select 1")
      val metadata = resultSet.getMetaData
      assert(metadata.getColumnName(1) == "default_catalog.default_database.tbl_a")
      assert(metadata.getColumnType(1) == java.sql.Types.BIGINT)
      assert(resultSet.next())
      assert(resultSet.getLong(1) == -1L)
    })
  }

  test("execute statement - set properties") {
    withMultipleConnectionJdbcStatement()({ statement =>
      val resultSet = statement.executeQuery("set table.dynamic-table-options.enabled = true")
      val metadata = resultSet.getMetaData
      assert(metadata.getColumnName(1) == "key")
      assert(metadata.getColumnName(2) == "value")
      assert(resultSet.next())
      assert(resultSet.getString(1) == "table.dynamic-table-options.enabled")
      assert(resultSet.getString(2) == "true")
    })
  }

  test("execute statement - show properties") {
    withMultipleConnectionJdbcStatement()({ statement =>
      val resultSet = statement.executeQuery("set")
      val metadata = resultSet.getMetaData
      assert(metadata.getColumnName(1) == "key")
      assert(metadata.getColumnName(2) == "value")
      assert(resultSet.next())
    })
  }

  test("execute statement - reset property") {
    withMultipleConnectionJdbcStatement()({ statement =>
      statement.executeQuery("set pipeline.jars = my.jar")
      statement.executeQuery("reset pipeline.jars")
      val resultSet = statement.executeQuery("set")
      // Flink does not support set key without value currently,
      // thus read all rows to find the desired one
      var success = false
      while (resultSet.next()) {
        if (resultSet.getString(1) == "pipeline.jars" && resultSet.getString(2) == "") {
          success = true
        }
      }
      assert(success)
    })
  }
}
