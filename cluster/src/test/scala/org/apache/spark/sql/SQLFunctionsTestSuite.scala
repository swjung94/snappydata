/*
 * Copyright (c) 2018 SnappyData, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package org.apache.spark.sql

import java.io.{File, FileOutputStream, PrintWriter}
import java.math.BigDecimal
import java.nio.file.{Files, Paths}
import java.sql.{Date, Timestamp}
import java.text.SimpleDateFormat
import java.util.Calendar

import scala.io.Source
import scala.language.postfixOps

import io.snappydata.SnappyFunSuite
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

import org.apache.spark.Logging
import org.apache.spark.sql.NorthWindDUnitTest.writeToFile
import org.apache.spark.sql.types._

class SQLFunctionsTestSuite extends SnappyFunSuite
    with Logging
    with BeforeAndAfter
    with BeforeAndAfterAll{

    // scalastyle:off println

    val sparkSession = SparkSession.builder().master("local[*]").getOrCreate()

    val pw = new PrintWriter(new FileOutputStream(
        new File("SQLFunctionTestSuite.out"), true))

    var query = ""

    override def beforeAll(): Unit = {
        super.beforeAll()
        createRowTable()
        createColumnTable()
        createSparkTable()
    }

    override def afterAll(): Unit = {
        super.afterAll()
        dropTables()
    }

    def createRowTable(): Unit = {
        snc.sql("CREATE TABLE rowTable (bigIntCol BIGINT," +
            " binaryCol1 BINARY," +
            " boolCol BOOLEAN ," +
            " byteCol BYTE," +
            " charCol CHAR( 30 )," +
            " dateCol DATE ," +
            " decimalCol DECIMAL( 11) ," +
            " doubleCol DOUBLE ," +
            " floatCol FLOAT ," +
            " intCol INT," +
            " integerCol INTEGER ," +
            " longVarcharCol LONG VARCHAR," +
            " numericCol NUMERIC," +
            " numeric1Col NUMERIC(10,2)," +
            " doublePrecisionCol DOUBLE PRECISION," +
            " realCol REAL," +
            " stringCol STRING," +
            " timestampCol TIMESTAMP," +
            " varcharCol VARCHAR( 20 ))")

        snc.sql("insert into rowtable values (1000, NULL, NULL, NULL," +
            " '1234567890abcdefghij', date('1970-01-08'), 66, 2.2, 1.0E8, 1000, 1000," +
            " '1234567890abcdefghij', 100000.0, 100000.0, 2.2, null, 'abcd'," +
            " timestamp('1997-01-01 03:03:03'), 'abcd')")

        snc.sql(s"insert into rowtable values (-10, NULL, true, NULL," +
            " 'ABC@#', current_date, -66, 0.0111, -2.225E-307, -10, 10," +
            " 'ABC@#', -1, 1, 123.56, 0.089, 'abcd', current_timestamp, 'SNAPPY')")
    }

    def createColumnTable(): Unit = {
        snc.sql("CREATE TABLE columnTable (bigIntCol BIGINT," +
            " binaryCol1 BINARY," +
            " boolCol BOOLEAN ," +
            " byteCol BYTE," +
            " charCol CHAR( 30 ) ," +
            " dateCol DATE ," +
            " decimalCol DECIMAL( 10, 2 ) ," +
            " doubleCol DOUBLE ," +
            " floatCol FLOAT ," +
            " intCol INT ," +
            " integerCol INTEGER," +
            " longVarcharCol LONG ," +
            " numericCol NUMERIC," +
            " numeric1Col NUMERIC(10,2)," +
            " doublePrecisionCol DOUBLE PRECISION," +
            " realCol REAL," +
            " stringCol STRING," +
            " timestampCol TIMESTAMP ," +
            " varcharCol VARCHAR( 20 )," +
            " arrayStringCol ARRAY<String>," +
            " arrayIntCol ARRAY<Integer>," +
            " mapCol MAP<INT, STRING>," +
            " structCol STRUCT<c1: STRING, c2: INTEGER>) using COLUMN options(BUCKETS '8')")

        snc.sql("insert into columntable select 1000, NULL, NULL, NULL," +
            " '1234567890abcdefghij', date('1970-01-08'), 66, 2.2, 1.0E8, 1000, 1000," +
            " '1234567890abcdefghij', 100000.0, 100000.0, 2.2, NULL," +
            " 'abcd', timestamp('1997-01-01 03:03:03'), 'abcd', NULL, NULL, NULL, NULL")

        snc.sql(s"insert into columntable select -10, NULL, true, NULL," +
            " 'ABC@#', current_date, -66, 0.0111, -2.225E-307, -10, 10," +
            " 'ABC@#', -1, 1, 123.56, 0.089, 'abcd', current_timestamp, 'SNAPPY'," +
            " Array('abc','def','efg'), Array(1,2,3), Map(1,'abc'), Struct('abc',123)")

    }

    def createSparkTable(): Unit = {

        val DecimalType = DataTypes.createDecimalType(10, 2)
        val now = Calendar.getInstance().getTime()
        val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
        val date1 = java.sql.Date.valueOf(dateFormat.format(Date.valueOf("1970-01-08")))
        val current_date = java.sql.Date.valueOf(dateFormat.format(now))
        val timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val time1 = java.sql.Timestamp.valueOf(
            timeFormat.format(Timestamp.valueOf("9999-12-31 23:59:59.999999")))
        val current_timestamp = java.sql.Timestamp.valueOf(timeFormat.format(now))
        // val str = "hello".getBytes()


        val schema = List(
            StructField("bigIntCol", IntegerType, true),
            StructField("binaryCol1", BinaryType, true),
            StructField("boolCol", BooleanType, true),
            StructField("byteCol", ByteType, true),
            StructField("charCol", StringType, true),
            StructField("dateCol", DateType, true),
            StructField("decimalCol", DecimalType, true),
            StructField("doubleCol", DoubleType, true),
            StructField("floatCol", FloatType, true),
            StructField("intCol", IntegerType, true),
            StructField("integerCol", IntegerType, true),
            StructField("longVarcharCol", StringType, true),
            StructField("numericCol", DecimalType, true),
            StructField("numeric1Col", DecimalType, true),
            StructField("doublePrecisionCol", DoubleType, true),
            StructField("realCol", FloatType, true),
            StructField("stringCol", StringType, true),
            StructField("timestampCol", TimestampType, true),
            StructField("varcharCol", StringType, true),
            StructField("arrayStringCol", ArrayType(StringType), true),
            StructField("arrayIntCol", ArrayType(IntegerType), true),
            StructField("mapCol", MapType(IntegerType, StringType), true),
            StructField("structCol", StructType(Seq(StructField("c1", StringType, false),
                StructField("c2", IntegerType, false))), true)
        )

        val data = Seq(
            Row(1000, null, null, null , "1234567890abcdefghij",
                date1 , new BigDecimal(66), 2.2, 1.0E8f,
                1000, 1000, "1234567890abcdefghij", new BigDecimal(100000.0),
                new BigDecimal(100000.0), 2.2 , null, "abcd",
                time1, "abcd'", null, null, null, null ),
            Row(-10, null, true, null , "ABC@#",
                current_date , new BigDecimal(-66), 0.0111, -2.225E-307f,
                -10, 10, "ABC@#", new BigDecimal(-1),
                new BigDecimal(1), 123.56 , 0.089f, "abcd",
                current_timestamp, "SNAPPY'", Array("abc", "def", "efg"),
                Array(1, 2, 3), scala.collection.immutable.Map(1 -> "abc"),
                Row("abc", 123))
            )

        val someDF = sparkSession.createDataFrame(
            sparkSession.sparkContext.parallelize(data),
            StructType(schema)
        )
        someDF.printSchema()
        someDF.createTempView("sparkTable")

        val sqlDF = sparkSession.sql("SELECT * FROM sparkTable")
        sqlDF.show()
    }

    def dropTables(): Unit = {
        snc.sql("DROP TABLE IF EXISTS rowTable")
        snc.sql("DROP TABLE IF EXISTS columnTable")
        sparkSession.sql("DROP TABLE IF EXISTS sparkTable")
    }

    def validateResult(sparkDf: DataFrame, snappyDf: DataFrame): Unit = {

        val sparkColumns = sparkDf.schema.fields.map(_.name)

        val selectiveDifferences = sparkColumns.map(col =>
            sparkDf.select(col).except(snappyDf.select(col)))
        sparkDf.show()
        snappyDf.show()
        println("selective difference " + selectiveDifferences)
        selectiveDifferences.map(diff => {
            println(diff)
            if (diff.count > 0) {
                diff.show()
                println(s"For query '$query' result mismatched observed")
            }
            else println(s"For query '$query' result matched observed")
        })


    }

    protected def getTempDir(dirName: String, onlyOnce: Boolean): String = {
        var log: File = new File(".")
        if (onlyOnce) {
            val logParent = log.getAbsoluteFile.getParentFile.getParentFile
            if (logParent.list().contains("output.txt")) {
                log = logParent
            } else if (logParent.getParentFile.list().contains("output.txt")) {
                log = logParent.getParentFile
            }
        }
        var dest: String = null
        dest = log.getCanonicalPath + File.separator + dirName
        val tempDir: File = new File(dest)
        if (!tempDir.exists) tempDir.mkdir()
        tempDir.getAbsolutePath
    }


    private def getSortedFiles(file: File): Array[File] = {
        file.getParentFile.listFiles.filter(_.getName.startsWith(file.getName)).sortBy { f =>
            val n = f.getName
            val i = n.lastIndexOf('.')
            n.substring(i + 1).toInt
        }
    }

    def assertQueryFullResultSet(snc: SnappyContext, sparkQuery: String,
        snappyQuery: String, numRows: Int,
        queryNum: String, tableType: String,
        pw: PrintWriter, sqlContext: SparkSession): Any = {

        var snappyDF = snc.sql(snappyQuery)
        val snappyQueryFileName = s"Snappy_$queryNum.out"
        val sparkQueryFileName = s"Spark_$queryNum.out"
        val snappyDest = getTempDir("snappyQueryFiles_" + tableType, onlyOnce = false)
        val sparkDest = getTempDir("sparkQueryFiles", onlyOnce = true)
        val sparkFile = new File(sparkDest, sparkQueryFileName)
        val snappyFile = new File(snappyDest, snappyQueryFileName)
        val col1 = snappyDF.schema.fieldNames(0)
        val col = snappyDF.schema.fieldNames.tail
        snappyDF = snappyDF.sort(col1, col: _*)
        writeToFile(snappyDF, snappyFile, snc)
        // scalastyle:off println
        pw.println(s"$queryNum Result Collected in files with prefix $snappyFile")
        if (!new File(s"$sparkFile").exists()) {
            var sparkDF = sqlContext.sql(sparkQuery)
            val col = sparkDF.schema.fieldNames(0)
            val cols = sparkDF.schema.fieldNames.tail
            sparkDF = sparkDF.sort(col, cols: _*)
            writeToFile(sparkDF, sparkFile, snc)
            pw.println(s"$queryNum Result Collected in files with prefix $sparkFile")
        }
        val expectedFiles = getSortedFiles(sparkFile).toIterator
        val actualFiles = getSortedFiles(snappyFile).toIterator
        val expectedLineSet = expectedFiles.flatMap(Source.fromFile(_).getLines())
        val actualLineSet = actualFiles.flatMap(Source.fromFile(_).getLines())
        var numLines = 0
        while (expectedLineSet.hasNext && actualLineSet.hasNext) {
            val expectedLine = expectedLineSet.next()
            val actualLine = actualLineSet.next()
            if (!actualLine.equals(expectedLine)) {
                pw.println(s"\n** For $queryNum result mismatch observed**")
                pw.println(s"\nExpected Result \n: $expectedLine")
                pw.println(s"\nActual Result   \n: $actualLine")
                pw.println(s"\nSnappy Query =" + snappyQuery + " Table Type : " + tableType)
                pw.println(s"\nSpark Query =" + sparkQuery + " Table Type : " + tableType)
                assert(false, s"\n** For $queryNum result mismatch observed** \n" +
                    s"Expected Result \n: $expectedLine \n" +
                    s"Actual Result   \n: $actualLine \n" +
                    s"Query =" + snappyQuery + " Table Type : " + tableType)
            }
            numLines += 1
        }
        if (actualLineSet.hasNext || expectedLineSet.hasNext) {
            pw.println(s"\nFor $queryNum result count mismatch observed")
            assert(false, s"\nFor $queryNum result count mismatch observed")
        }
        assert(numLines == numRows, s"\nFor $queryNum result count mismatch " +
            s"observed: Expected=$numRows, Got=$numLines")
        pw.flush()
        val snFile: String = snappyFile.toString + ".0"
        val spFile: String = sparkFile.toString + ".0"
        println("Query executed successfully" + snFile + " " + spFile)
        Files.delete(Paths.get(snFile))
        println(snappyFile.toString + " file deleted")
        Files.delete(Paths.get(spFile))
        println(sparkFile.toString + " file deleted")

    }

    test("abs"){

        query = "select abs(-1)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "select abs(1)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))


        //  ERROR: org.apache.spark.sql.AnalysisException:
        // cannot resolve '`abs(1.1)`' given input columns: [abs(1.1)];;
        query = "select abs(1.1)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "select abs(-1.1)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "select abs(0.0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

    }

    test("coalesce"){
        // ERROR: org.apache.spark.sql.AnalysisException:
        // cannot resolve '`coalesce(CAST(NULL AS STRING), CAST(NULL AS STRING),
        // CAST(NULL AS STRING), CAST(abc AS STRING), CAST(NULL AS STRING),
        // CAST(Example.com AS STRING))`' given input columns: [coalesce(CAST(NULL AS STRING),
        // CAST(NULL AS STRING), CAST(NULL AS STRING), CAST(abc AS STRING),
        // CAST(NULL AS STRING), CAST(Example.com AS STRING))];;
        query = "SELECT COALESCE(NULL,NULL,NULL,'abc',NULL,'Example.com')"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT COALESCE(NULL, 1, 2, 'abc')"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)



        // val c1s = snappyDf.columns
        // val c2s = snappyDf1.columns
        // assert(!c1s.sameElements(c2s))


        query = "SELECT COALESCE(1, 2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        //  with validateResult(sparkDf, snappyDf) throws error
        query = "SELECT COALESCE(NULL, NULL)"
        // sparkDf = sparkSession.sql(s"$query")
        // snappyDf = snc.sql(s"$query")
        // validateResult(sparkDf, snappyDf)
        assertQueryFullResultSet(snc, query, query, 1,
        "coalesce_q3", " ", pw, sparkSession)

    }

    test("cast"){
        // ERROR: below all queries throws error
        // org.apache.spark.sql.AnalysisException:
        // cannot resolve '`CAST(NaN AS DOUBLE)`'
        // given input columns: [CAST('NaN' AS DOUBLE)];;

        // On snappy shell for below query throws error
        // snappy> select cast('NaN' as double);
        // ERROR 22003: (SQLState=22003 Severity=20000)
        // (Server=localhost/127.0.0.1[1528] Thread=ThriftProcessor-0)
        // The resulting value is outside the range for data type 'DOUBLE' column 'null'.
        query = "select cast('NaN' as double)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT CAST(25.65 AS varchar(12))"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT cast('10' as int)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT CAST('2017-08-25' AS date)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

    }

    test("explode"){
        query = "SELECT explode(array(10, 20))"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT explode(array(0))"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT explode(array(NULL,1))"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("greatest"){
        query = "SELECT greatest(10, 9, 2, 4, 3)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT greatest(0, NULL)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("if"){
        // ERROR: org.apache.spark.sql.AnalysisException:
        // cannot resolve '`(IF((1 < 2), a, b))`'
        // given input columns: [(IF((1 < 2), 'a', 'b'))];;
        query = "SELECT if(1 < 2, 'a', 'b')"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT if(0 < NULL, 'a', 'b')"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("inline"){
        query = "SELECT inline(array(struct(1, 'a'), struct(2, 'b')))"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT inline(array(struct(1), struct(2)))"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("isnan"){
        // ERROR: org.apache.spark.sql.AnalysisException:
        // cannot resolve '`isnan(CAST(NaN AS DOUBLE))`'
        // given input columns: [isnan(CAST('NaN' AS DOUBLE))];;
        query = "SELECT isnan(cast('NaN' as double))"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT isnan(123)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("ifnull"){
        query = "SELECT ifnull(NULL, array('2'))"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT ifnull(2, 3)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("isnull"){
        query = "SELECT isnull(1)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException:
        // cannot resolve '`(abc IS NULL)`' given input columns: [('abc' IS NULL)];;
        query = "SELECT isnull('abc')"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT isnull(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("isnotnull"){
        query = "SELECT isnotnull(1)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException:
        // cannot resolve '`(abc IS NOT NULL)`'
        // given input columns: [('abc' IS NOT NULL)];;
        query = "SELECT isnotnull('abc')"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT isnotnull(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("least"){
        query = "SELECT least(10, 9, 2, 4, 3)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT least(null, 9, 3)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("nanvl"){
        // ERROR: org.apache.spark.sql.AnalysisException:
        // cannot resolve '`nanvl(CAST(NaN AS DOUBLE), CAST(123 AS DOUBLE))`'
        // given input columns: [nanvl(CAST('NaN' AS DOUBLE), CAST(123 AS DOUBLE))];;
        query = "SELECT nanvl(cast('NaN' as double), 123)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // On snappy shell throws error for below query
        // snappy> SELECT nanvl(cast('NaN' as double), cast('NaN' as double));
        // ERROR 22003: (SQLState=22003 Severity=20000)
        // (Server=localhost/127.0.0.1[1528] Thread=ThriftProcessor-0)
        // The resulting value is outside the range for data type 'DOUBLE' column 'null'.
        query = "SELECT nanvl(cast('NaN' as double), cast('NaN' as double))"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        // snappy> SELECT nanvl('NaN','NaN');
        // ERROR 22003: (SQLState=22003 Severity=20000)
        // (Server=localhost/127.0.0.1[1528] Thread=ThriftProcessor-0)
        // The resulting value is outside the range for data type 'DOUBLE' column 'null'.
        query = "SELECT nanvl('NaN','NaN')"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

    }

    test("nullif"){
        query = "SELECT nullif(2, 2)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT nullif( 9, 3)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT nullif( 9, 9, 4)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT nullif( 9, 9, 9)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

    }

    test("nvl"){
        query = "SELECT nvl(NULL, array('2'))"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT nvl( 9, 3)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("nvl2"){
        query = "SELECT nvl2(NULL, 2, 1)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT nvl2( 9, 3, 1)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("posexplode"){
        query = "SELECT posexplode(array(10,20))"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT posexplode(array(10,0,null))"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("rand") {
        query = "select rand()"
        var snappyDf = snc.sql(s"$query")
        snappyDf.show()

        query = "select rand(null)"
        var snappyDf1 = snc.sql(s"$query")
        snappyDf1.show()

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        // Throws error on snappy shell as well as in test
        // snappy> select rand(0);
        // ERROR 42000: (SQLState=42000 Severity=20000)
        // (Server=localhost/127.0.0.1[1528] Thread=ThriftProcessor-0)
        // Syntax error or analysis exception: Input argument
        // to rand must be an integer, long or null literal.;
        query = "select rand(0)"
        snappyDf = snc.sql(s"$query")
        snappyDf.show()

        // Throws error on snappy shell as well as in test
        // snappy> select rand(2);
        // ERROR 42000: (SQLState=42000 Severity=20000)
        // (Server=localhost/127.0.0.1[1528] Thread=ThriftProcessor-0)
        // Syntax error or analysis exception: Input argument
        // to rand must be an integer, long or null literal.;
        query = "select rand(2)"
        snappyDf = snc.sql(s"$query")
        snappyDf.show()



    }

    test("randn") {
        query = "select randn()"
        var snappyDf = snc.sql(s"$query")
        snappyDf.show()

        query = "select randn(null)"
        var snappyDf1 = snc.sql(s"$query")
        snappyDf1.show()

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        // Throws error on snappy shell as well as in test
        // snappy> select randn(0);
        // ERROR 42000: (SQLState=42000 Severity=20000)
        // (Server=localhost/127.0.0.1[1528] Thread=ThriftProcessor-0)
        // Syntax error or analysis exception: Input argument
        // to randn must be an integer, long or null literal.;
        query = "select randn(0)"
        snappyDf = snc.sql(s"$query")
        snappyDf.show()

        // Throws error on snappy shell as well as in test
        // snappy> select randn(2);
        // ERROR 42000: (SQLState=42000 Severity=20000)
        // (Server=localhost/127.0.0.1[1528] Thread=ThriftProcessor-0)
        // Syntax error or analysis exception: Input argument
        // to randn must be an integer, long or null literal.;
        query = "select randn(2)"
        snappyDf = snc.sql(s"$query")
        snappyDf.show()


    }

    test("stack"){
        query = "SELECT stack(2, 1, 2, 3)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // Throws error on snappy shell as well as in test
        // snappy> SELECT stack(2, 1, 2, 3, 4);
        // ERROR 42X01: (SQLState=42X01 Severity=20000)
        // (Server=localhost/127.0.0.1[1528] Thread=ThriftProcessor-0)
        // Syntax error: java.lang.AssertionError: assertion failed;.
        // Issue the 'help' command for general information on SnappyData command syntax.
        // Any unrecognized commands are treated as potential SQL commands and executed directly.
        // Consult your DBMS server reference documentation for
        // details of the SQL syntax supported by your server.
        query = "SELECT stack(2, 1, 2, 3, 4)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("when"){
        query = "SELECT case when 2>1 then 2 else 1 end"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT case when 2<1 then 1 else 2 end"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("acos"){
       // On snappy shell throws below error
       // snappy> select acos(2);
       // ERROR 22003: (SQLState=22003 Severity=20000)
       // (Server=localhost/127.0.0.1[1528] Thread=ThriftProcessor-0)
       // The resulting value is outside the range for data type 'DOUBLE' column 'null'.
       query = "select acos(2)"
       var sparkDf = sparkSession.sql(s"$query")
       var snappyDf = snc.sql(s"$query")
       validateResult(sparkDf, snappyDf)

        query = "SELECT acos(1)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT acos(-1)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT acos(0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT acos(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`ACOS(CAST(2.2 AS DOUBLE))`' given input columns: [ACOS(CAST(2.2 AS DOUBLE))];;
        query = "SELECT acos(2.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
   }

   test("asin"){
       query = "SELECT asin(0)"
       var sparkDf = sparkSession.sql(s"$query")
       var snappyDf = snc.sql(s"$query")
       validateResult(sparkDf, snappyDf)

       // On snappy shell throws below error
       // snappy> SELECT asin(2);
       // ERROR 22003: (SQLState=22003 Severity=20000)
       // (Server=localhost/127.0.0.1[1528] Thread=ThriftProcessor-0)
       // The resulting value is outside the range for data type 'DOUBLE' column 'null'.
       query = "SELECT asin(2)"
       sparkDf = sparkSession.sql(s"$query")
       var snappyDf1 = snc.sql(s"$query")
       validateResult(sparkDf, snappyDf1)

       val c1s = snappyDf.columns
       val c2s = snappyDf1.columns
       assert(!c1s.sameElements(c2s))

       query = "SELECT asin(-2)"
       sparkDf = sparkSession.sql(s"$query")
       snappyDf = snc.sql(s"$query")
       validateResult(sparkDf, snappyDf)

       query = "SELECT asin(null)"
       sparkDf = sparkSession.sql(s"$query")
       snappyDf = snc.sql(s"$query")
       validateResult(sparkDf, snappyDf)

       // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
       // '`ASIN(CAST(2.2 AS DOUBLE))`' given input columns: [ASIN(CAST(2.2 AS DOUBLE))];;
       query = "SELECT asin(2.2)"
       sparkDf = sparkSession.sql(s"$query")
       snappyDf = snc.sql(s"$query")
       validateResult(sparkDf, snappyDf)
   }

   test("atan"){
       query = "SELECT atan(0)"
       var sparkDf = sparkSession.sql(s"$query")
       var snappyDf = snc.sql(s"$query")
       validateResult(sparkDf, snappyDf)

       query = "SELECT atan(2)"
       sparkDf = sparkSession.sql(s"$query")
       var snappyDf1 = snc.sql(s"$query")
       validateResult(sparkDf, snappyDf1)

       val c1s = snappyDf.columns
       val c2s = snappyDf1.columns
       assert(!c1s.sameElements(c2s))

       query = "SELECT atan(-2)"
       sparkDf = sparkSession.sql(s"$query")
       snappyDf = snc.sql(s"$query")
       validateResult(sparkDf, snappyDf)

       query = "SELECT atan(null)"
       sparkDf = sparkSession.sql(s"$query")
       snappyDf = snc.sql(s"$query")
       validateResult(sparkDf, snappyDf)

       // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
       // '`ATAN(CAST(2.2 AS DOUBLE))`' given input columns: [ATAN(CAST(2.2 AS DOUBLE))];;
       query = "SELECT atan(2.2)"
       sparkDf = sparkSession.sql(s"$query")
       snappyDf = snc.sql(s"$query")
       validateResult(sparkDf, snappyDf)
   }

    test("atan2"){
        query = "SELECT atan2(0, 0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT atan2(2, 3)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT atan2(2, null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`ATAN2(CAST(2.2 AS DOUBLE), CAST(3 AS DOUBLE))`' given input columns:
        // [ATAN2(CAST(2.2 AS DOUBLE), CAST(3 AS DOUBLE))];;
        query = "SELECT atan2(2.2, 3)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("bin"){
        query = "SELECT bin(13)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT bin(-13)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT bin(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`bin(CAST(13.3 AS BIGINT))`' given input columns: [bin(CAST(13.3 AS BIGINT))];;
        query = "SELECT bin(13.3)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("bround"){

        // ERROR: below all queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`bround(2.5, 0)`' given input columns: [bround(2.5, 0)];;
        // 'Project ['bround(2.5, 0)]
        query = "SELECT bround(2.5, 0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT bround(2.5, 3)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT bround(2.5, null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT round(0, null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("cbrt"){

        query = "SELECT cbrt(25)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT cbrt(0)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT cbrt(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`CBRT(CAST(27.0 AS DOUBLE))`' given input columns: [CBRT(CAST(27.0 AS DOUBLE))];;
        query = "SELECT cbrt(27.0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("ceil"){
        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`CEIL(-0.1)`' given input columns: [CEIL(-0.1)];;
        // 'Project ['CEIL(-0.1)]
        query = "SELECT ceil(-0.1)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT ceil(5)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT ceil(0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT ceil(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("ceiling"){
        // ERROR:  org.apache.spark.sql.AnalysisException: cannot resolve
        // '`CEIL(-0.1)`' given input columns: [CEIL(-0.1)];;
        // 'Project ['CEIL(-0.1)]
        query = "SELECT ceiling(-0.1)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT ceiling(5)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT ceiling(0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT ceiling(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("cos"){

        query = "SELECT cos(0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT cos(2)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT cos(-2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT cos(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`COS(CAST(2.2 AS DOUBLE))`' given input columns: [COS(CAST(2.2 AS DOUBLE))];;
        query = "SELECT cos(2.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("cosh"){

        query = "SELECT cosh(0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT cosh(2)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT cosh(-2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT cosh(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`COSH(CAST(2.2 AS DOUBLE))`' given input columns: [COSH(CAST(2.2 AS DOUBLE))];;
        query = "SELECT cosh(2.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("conv"){

        // ERROR: below all queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`conv(100, 2, 10)`' given input columns: [conv('100', 2, 10)];;
        query = "SELECT conv('100', 2, 10)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT conv(-10, 16, -10)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("degrees"){
        // ERROR: below all queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`DEGREES(CAST(3.141592653589793 AS DOUBLE))`' given input columns:
        // [DEGREES(CAST(3.141592653589793 AS DOUBLE))];;
        query = "SELECT degrees(3.141592653589793)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT degrees(6.283185307179586 )"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT degrees(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT degrees(0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("e"){
        query = "SELECT e()"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("exp"){
        query = "SELECT exp(0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT exp(2)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT exp(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("expm1"){
        query = "SELECT expm1(0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT expm1(2)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT expm1(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("floor"){

        query = "SELECT floor(5)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT floor(null)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT floor(0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR:  org.apache.spark.sql.AnalysisException: cannot resolve
        // '`FLOOR(-0.1)`' given input columns: [FLOOR(-0.1)];;
        // 'Project ['FLOOR(-0.1)]
        query = "SELECT floor(-0.1)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("factorial"){
        query = "SELECT factorial(5)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT factorial(-5)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT factorial(0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT factorial(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("hex"){

        query = "SELECT hex(17)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT hex(0)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT hex(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`hex(Spark SQL)`' given input columns: [hex('Spark SQL')];;
        // 'Project ['hex(Spark SQL)]
        query = "SELECT hex('Spark SQL')"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)


    }

    test("hypot"){

        query = "SELECT hypot(3, 4)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT hypot(7,8)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT hypot(0,0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT hypot(0,null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT hypot(null,null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("log"){

        query = "SELECT log(10, 100)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT log(10,1000)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT log(10,0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT log(10,null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`LOG(CAST(10 AS DOUBLE), CAST(1000.234 AS DOUBLE))`'
        // given input columns: [LOG(CAST(10 AS DOUBLE), CAST(1000.234 AS DOUBLE))];;
        query = "SELECT log(10, 1000.234)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("log10"){

        query = "SELECT log10(10)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT log10(0)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT log10(-2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT log10(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`LOG10(CAST(1.2 AS DOUBLE))`' given input columns: [LOG10(CAST(1.2 AS DOUBLE))];;
        query = "SELECT log10(1.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

    }

    test("log1p"){

        query = "SELECT log1p(0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT log1p(2)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT log1p(-2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`LOG1P(CAST(1.2 AS DOUBLE))`' given input columns: [LOG1P(CAST(1.2 AS DOUBLE))];;
        query = "SELECT log1p(1.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT log1p(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("log2"){
        query = "SELECT log2(0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT log2(2)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT log2(-2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`LOG2(CAST(1.2 AS DOUBLE))`' given input columns: [LOG2(CAST(1.2 AS DOUBLE))];;
        query = "SELECT log2(1.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT log2(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("ln"){
        query = "SELECT ln(0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT ln(1)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT ln(-1)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`LOG(CAST(1.2 AS DOUBLE))`' given input columns: [LOG(CAST(1.2 AS DOUBLE))];;
        query = "SELECT ln(1.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT ln(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("negative"){
        query = "SELECT negative(0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT negative(1)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT negative(-1)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT negative(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`(- 1.2)`' given input columns: [(- 1.2)];;
        query = "SELECT negative(1.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT negative(-1.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

    }

    test("pi"){
        query = "SELECT pi()"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("pmod"){
        query = "SELECT pmod(10,3)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT pmod(-10,3)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT pmod(0,3)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT pmod(null,3)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`pmod(CAST(1.2 AS DECIMAL(11,1)), CAST(CAST(3 AS DECIMAL(10,0))
        // AS DECIMAL(11,1)))`' given input columns:
        // [pmod(CAST(1.2 AS DECIMAL(11,1)), CAST(CAST(3 AS DECIMAL(10,0)) AS DECIMAL(11,1)))];;
        query = "SELECT pmod(1.2,3)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("positive"){
        query = "SELECT positive(0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT positive(1)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT positive(-1)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT positive(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`(+ 1.2)`' given input columns: [(+ 1.2)];;
        // 'Project ['(+ 1.2)]
        query = "SELECT positive(1.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT positive(-1.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

    }

    test("pow"){
        query = "SELECT pow(3,2)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT pow(-10,3)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT pow(0,3)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT pow(null,3)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR:  org.apache.spark.sql.AnalysisException: cannot resolve
        // '`POWER(CAST(1.2 AS DOUBLE), CAST(3 AS DOUBLE))`'
        // given input columns: [POWER(CAST(1.2 AS DOUBLE), CAST(3 AS DOUBLE))];;
        query = "SELECT pow(1.2,3)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("power"){
        query = "SELECT power(3,2)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT power(-10,3)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT power(0,3)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT power(null,3)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR:  org.apache.spark.sql.AnalysisException: cannot resolve
        // '`POWER(CAST(1.2 AS DOUBLE), CAST(3 AS DOUBLE))`'
        // given input columns: [POWER(CAST(1.2 AS DOUBLE), CAST(3 AS DOUBLE))];;
        query = "SELECT power(1.2,3)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("radians"){

        // ERROR: below all queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`RADIANS(CAST(360.0 AS DOUBLE))`' given input columns:
        // [RADIANS(CAST(360.0 AS DOUBLE))];;
        query = "SELECT radians(360.0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT radians(180)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT radians(0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT radians(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("rint"){
        // ERROR: below all queries throws error:
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`ROUND(CAST(12.3456 AS DOUBLE))`' given input columns:
        // [ROUND(CAST(12.3456 AS DOUBLE))];;
        query = "SELECT rint(12.3456)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT rint(-12.3456)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT rint(180)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT rint(0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT rint(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("round"){

        // ERROR: below all queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`round(2.5, 0)`' given input columns: [round(2.5, 0)];;
        // 'Project ['round(2.5, 0)]
        query = "SELECT round(2.5, 0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT round(2.5, 3)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT round(2.5, null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT round(0, null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("shiftleft"){

        query = "SELECT shiftleft(4, 1)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT shiftleft(0, 1)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT shiftleft(null, null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: below both queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`shiftleft(CAST(2.2 AS INT), 2)`' given input columns:
        // [shiftleft(CAST(2.2 AS INT), 2)];;
        query = "SELECT shiftleft(2.2, 2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT shiftleft(2.2, 0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("shiftright"){

        query = "SELECT shiftright(4, 1)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT shiftright(0, 1)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT shiftright(null, null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: below both queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`shiftright(CAST(2.2 AS INT), 2)`' given input columns:
        // [shiftright(CAST(2.2 AS INT), 2)];;
        query = "SELECT shiftright(2.2, 2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT shiftright(2.2, 0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("shiftrightunsigned"){

        query = "SELECT shiftrightunsigned(4, 1)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT shiftrightunsigned(0, 1)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT shiftrightunsigned(null, null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: below both queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`shiftrightunsigned(CAST(2.2 AS INT), 2)`' given input columns:
        // [shiftrightunsigned(CAST(2.2 AS INT), 2)];;
        query = "SELECT shiftrightunsigned(2.2, 2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT shiftrightunsigned(2.2, 0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("sign"){
        query = "SELECT sign(40)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT sign(-40)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT sign(0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT sign(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`SIGNUM(CAST(-4.20 AS DOUBLE))`' given input columns:
        // [SIGNUM(CAST(-4.20 AS DOUBLE))];;
        query = "SELECT sign(-4.20)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("signum"){
        query = "SELECT signum(40)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT signum(-40)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT signum(0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT signum(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`SIGNUM(CAST(-4.20 AS DOUBLE))`' given input columns:
        // [SIGNUM(CAST(-4.20 AS DOUBLE))];;
        query = "SELECT signum(-4.20)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("sin"){
        query = "SELECT sin(0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT sin(2)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT sin(-2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT sin(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`SIN(CAST(2.2 AS DOUBLE))`' given input columns:
        // [SIN(CAST(2.2 AS DOUBLE))];;
        query = "SELECT sin(2.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("sinh"){
        query = "SELECT sinh(0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT sinh(2)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT sinh(-2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT sinh(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`SINH(CAST(2.2 AS DOUBLE))`' given input columns:
        // [SINH(CAST(2.2 AS DOUBLE))];;
        query = "SELECT sinh(2.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("str_to_map"){

        query = "SELECT str_to_map(null)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // throws below error
        // org.apache.spark.sql.AnalysisException:
        // Cannot have map type columns in DataFrame which calls set
        // operations(intersect, except, etc.), but the type of
        // column str_to_map(CAST(NULL AS STRING), ,, :) is map<string,string>;;
       query = "SELECT str_to_map('a:1,b:2,c:3', ',', ':')"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT str_to_map('a')"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT str_to_map('-1.2:a')"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT str_to_map(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("sqrt"){

        query = "SELECT sqrt(4)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // On snappy shell throws below error for this query
        // snappy> select sqrt(-4);
        // ERROR 22003: (SQLState=22003 Severity=20000)
        // (Server=localhost/127.0.0.1[1528] Thread=ThriftProcessor-1)
        // The resulting value is outside the range for data type 'DOUBLE' column 'null'.
        query = "SELECT sqrt(-4)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT sqrt(0)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT sqrt(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`SQRT(CAST(4.4 AS DOUBLE))`' given input columns:
        // [SQRT(CAST(4.4 AS DOUBLE))];;
        query = "SELECT sqrt(4.4)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("tan"){

        query = "SELECT tan(0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT tan(2)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT tan(-2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT tan(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: below both queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`TAN(CAST(2.2 AS DOUBLE))`' given input columns:
        // [TAN(CAST(2.2 AS DOUBLE))];;
        query = "SELECT tan(2.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT tan(-2.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("tanh"){

        query = "SELECT tanh(0)"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT tanh(2)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT tanh(-2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT tanh(null)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: below both queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`TANH(CAST(2.2 AS DOUBLE))`' given input columns:
        // [TANH(CAST(2.2 AS DOUBLE))];;
        query = "SELECT tanh(2.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT tanh(-2.2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("+"){
        query = "SELECT (1+1)+3"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`((CAST(1.2 AS DECIMAL(12,1)) + CAST(CAST(3 AS DECIMAL(10,0))
        // AS DECIMAL(12,1))) + (CAST(4.5 AS DECIMAL(12,1)) +
        // CAST(CAST(2 AS DECIMAL(10,0)) AS DECIMAL(12,1))))`'
        // given input columns: [((CAST(1.2 AS DECIMAL(12,1)) +
        // CAST(CAST(3 AS DECIMAL(10,0)) AS DECIMAL(12,1))) +
        // (CAST(4.5 AS DECIMAL(12,1)) + CAST(CAST(2 AS DECIMAL(10,0))
        // AS DECIMAL(12,1))))];;
        query = "SELECT 1.2+3+(4.5+2)"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT 0+0"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT 0+null"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("-"){
        query = "SELECT 1-1-1"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT 0-0"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT 0-null"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`((CAST(1.2 AS DECIMAL(12,1)) - CAST(CAST(3 AS DECIMAL(10,0))
        // AS DECIMAL(12,1))) - (CAST(4.5 AS DECIMAL(12,1)) -
        // CAST(CAST(2 AS DECIMAL(10,0)) AS DECIMAL(12,1))))`'
        // given input columns: [((CAST(1.2 AS DECIMAL(12,1)) -
        // CAST(CAST(3 AS DECIMAL(10,0)) AS DECIMAL(12,1))) -
        // (CAST(4.5 AS DECIMAL(12,1)) - CAST(CAST(2 AS DECIMAL(10,0))
        // AS DECIMAL(12,1))))];;
        query = "SELECT 1.2-3-(4.5-2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("*"){
        query = "SELECT 4*2"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT 0*0"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT 0*null"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException:
        // cannot resolve '`((CAST(1.2 AS DECIMAL(11,1)) *
        // CAST(CAST(3 AS DECIMAL(10,0)) AS DECIMAL(11,1))) *
        // (CAST(4.5 AS DECIMAL(11,1)) * CAST(CAST(2 AS DECIMAL(10,0))
        // AS DECIMAL(11,1))))`' given input columns: [((CAST(1.2 AS DECIMAL(11,1))
        // * CAST(CAST(3 AS DECIMAL(10,0)) AS DECIMAL(11,1))) *
        // (CAST(4.5 AS DECIMAL(11,1)) * CAST(CAST(2 AS DECIMAL(10,0))
        // AS DECIMAL(11,1))))];;
        query = "SELECT 1.2*3*(4.5*2)"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("/"){
        query = "SELECT 4/2"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT 0/0"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT 0/null"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException:
        // cannot resolve '`(CAST(4.5 AS DECIMAL(11,1)) /
        // CAST(CAST(2 AS DECIMAL(10,0)) AS DECIMAL(11,1)))`'
        // given input columns: [(CAST(4.5 AS DECIMAL(11,1)) /
        // CAST(CAST(2 AS DECIMAL(10,0)) AS DECIMAL(11,1)))];;
        query = "SELECT 4.5/2"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("%"){
        query = "SELECT 4%2"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT 0%0"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        query = "SELECT 0%null"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        // ERROR: org.apache.spark.sql.AnalysisException:
        // cannot resolve '`(CAST(4.5 AS DECIMAL(11,1)) %
        // CAST(CAST(2 AS DECIMAL(10,0)) AS DECIMAL(11,1)))`'
        // given input columns: [(CAST(4.5 AS DECIMAL(11,1)) %
        // CAST(CAST(2 AS DECIMAL(10,0)) AS DECIMAL(11,1)))];;
        query = "SELECT 4.5%2"
        sparkDf = sparkSession.sql(s"$query")
        snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)
    }

    test("avg"){
        var sparkQuery = "SELECT avg(intcol) from sparktable"
        var snappyQuery = "SELECT avg(intcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "avg_q1", "RowTable", pw, sparkSession)
        /* var sparkDf = sparkSession.sql(s"$sparkQuery")
        var snappyDf = snc.sql(s"$snappyQuery")
        var sparkDf1 = sparkDf
        validateResult(sparkDf1, snappyDf) */

        sparkQuery = "SELECT avg(intcol) from sparktable"
        snappyQuery = "SELECT avg(intcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "avg_q2", "ColumnTable", pw, sparkSession)
    }

    test("count"){
        var sparkQuery = "SELECT count(*) from sparktable"
        var snappyQuery = "SELECT count(*) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "count_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT count(intcol) from sparktable"
        var snappyQuery1 = "SELECT count(intcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery1, 1,
            "count_q2", "RowTable", pw, sparkSession)

        var snappyDf = snc.sql(s"$snappyQuery")
        var snappyDf1 = snc.sql(s"$snappyQuery1")

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        sparkQuery = "SELECT count(distinct(intcol)) from sparktable"
        snappyQuery = "SELECT count(distinct(intcol)) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "count_q3", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT count(*) from sparktable"
        snappyQuery = "SELECT count(*) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "count_q4", "ColumnTable", pw, sparkSession)

        sparkQuery = "SELECT count(intcol) from sparktable"
        snappyQuery = "SELECT count(intcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "count_q5", "ColumnTable", pw, sparkSession)

        sparkQuery = "SELECT count(distinct(intcol)) from sparktable"
        snappyQuery = "SELECT count(distinct(intcol)) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "count_q6", "ColumnTable", pw, sparkSession)
    }

    test("first"){
        var sparkQuery = "SELECT first(stringcol) from sparktable"
        var snappyQuery = "SELECT first(stringcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "first_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT first(stringcol, true) from sparktable"
        var snappyQuery1 = "SELECT first(stringcol, true) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery1, 1,
            "first_q2", "RowTable", pw, sparkSession)

        var snappyDf = snc.sql(s"$snappyQuery")
        var snappyDf1 = snc.sql(s"$snappyQuery1")

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        sparkQuery = "SELECT first(stringcol) from sparktable"
        snappyQuery = "SELECT first(stringcol) from columntable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "first_q3", "ColumnTable", pw, sparkSession)

        sparkQuery = "SELECT first(stringcol, true) from sparktable"
        snappyQuery = "SELECT first(stringcol, true) from columntable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "first_q4", "ColumnTable", pw, sparkSession)
    }

    test("first_value"){
        var sparkQuery = "SELECT first_value(stringcol) from sparktable"
        var snappyQuery = "SELECT first_value(stringcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "first_value_q1", "RowTable", pw, sparkSession)

        // throws below error
        //  org.apache.spark.sql.AnalysisException:
        // The second argument of First should be a boolean literal.;;
        sparkQuery = "SELECT first_value(stringcol, true) from sparktable"
        var snappyQuery1 = "SELECT first_value(stringcol, true) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery1, 1,
            "first_value_q2", "RowTable", pw, sparkSession)

        var snappyDf = snc.sql(s"$snappyQuery")
        var snappyDf1 = snc.sql(s"$snappyQuery1")

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        sparkQuery = "SELECT first_value(stringcol) from sparktable"
        snappyQuery = "SELECT first_value(stringcol) from columntable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "first_value_q3", "ColumnTable", pw, sparkSession)

        // throws below error
        //  org.apache.spark.sql.AnalysisException:
        // The second argument of First should be a boolean literal.;;
        sparkQuery = "SELECT first_value(stringcol, true) from sparktable"
        snappyQuery = "SELECT first_value(stringcol, true) from columntable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "first_value_q4", "ColumnTable", pw, sparkSession)
    }

    test("last"){
        var sparkQuery = "SELECT last(stringcol) from sparktable"
        var snappyQuery = "SELECT last(stringcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "last_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT last(stringcol, true) from sparktable"
        var snappyQuery1 = "SELECT last(stringcol, true) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery1, 1,
            "last_q2", "RowTable", pw, sparkSession)

        var snappyDf = snc.sql(s"$snappyQuery")
        var snappyDf1 = snc.sql(s"$snappyQuery1")

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        sparkQuery = "SELECT last(stringcol) from sparktable"
        snappyQuery = "SELECT last(stringcol) from columntable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "last_q3", "ColumnTable", pw, sparkSession)

        sparkQuery = "SELECT last(stringcol, true) from sparktable"
        snappyQuery = "SELECT last(stringcol, true) from columntable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "last_q4", "ColumnTable", pw, sparkSession)
    }

    test("last_value"){
        var sparkQuery = "SELECT last_value(stringcol) from sparktable"
        var snappyQuery = "SELECT last_value(stringcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "last_value_q1", "RowTable", pw, sparkSession)

        // throws below error
        // snappy> SELECT last_value(stringcol, true) from columntable;
        // ERROR 42000: (SQLState=42000 Severity=20000)
        // (Server=localhost/127.0.0.1[1528] Thread=ThriftProcessor-0)
        // Syntax error or analysis exception:
        // The second argument of First should be a boolean literal.;;
        sparkQuery = "SELECT last_value(stringcol, true) from sparktable"
        var snappyQuery1 = "SELECT last_value(stringcol, true) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery1, 1,
            "last_value_q2", "RowTable", pw, sparkSession)

        var snappyDf = snc.sql(s"$snappyQuery")
        var snappyDf1 = snc.sql(s"$snappyQuery1")

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))

        sparkQuery = "SELECT last_value(stringcol) from sparktable"
        snappyQuery = "SELECT last_value(stringcol) from columntable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "last_value_q3", "ColumnTable", pw, sparkSession)

        // throws below error
        //  org.apache.spark.sql.AnalysisException:
        // The second argument of last should be a boolean literal.;;
        sparkQuery = "SELECT last_value(stringcol, true) from sparktable"
        snappyQuery = "SELECT last_value(stringcol, true) from columntable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "last_value_q4", "ColumnTable", pw, sparkSession)
    }

    test("max") {
        var sparkQuery = "SELECT max(intcol) from sparktable"
        var snappyQuery = "SELECT max(intcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "max_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT max(intcol) from sparktable"
        snappyQuery = "SELECT max(intcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "max_q2", "ColumnTable", pw, sparkSession)
    }

    test("min"){
        var sparkQuery = "SELECT min(intcol) from sparktable"
        var snappyQuery = "SELECT min(intcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "min_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT min(intcol) from sparktable"
        snappyQuery = "SELECT min(intcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "min_q2", "ColumnTable", pw, sparkSession)
    }

    test("sum"){
        var sparkQuery = "SELECT sum(intcol) from sparktable"
        var snappyQuery = "SELECT sum(intcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "sum_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT sum(intcol) from sparktable"
        snappyQuery = "SELECT sum(intcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "sum_q2", "ColumnTable", pw, sparkSession)
    }

    test("length"){
        var sparkQuery = "SELECT length(stringcol) from sparktable"
        var snappyQuery = "SELECT length(stringcol) from columntable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "length_q1", "ColumnTable", pw, sparkSession)

        sparkQuery = "SELECT length(stringcol) from sparktable"
        snappyQuery = "SELECT length(stringcol) from rowTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "length_q1", "RowTable", pw, sparkSession)

        // ERROR: org.apache.spark.sql.AnalysisException:
        // cannot resolve '`length(Spark SQL)`'
        // given input columns: [length('Spark SQL')];;
        query = "SELECT length('Spark SQL')"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)
    }

    test("lower"){
        var sparkQuery = "SELECT lower(stringcol) from sparktable"
        var snappyQuery = "SELECT lower(stringcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "lower_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT lower(stringcol) from sparktable"
        snappyQuery = "SELECT lower(stringcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "lower_q2", "ColumnTable", pw, sparkSession)

        // ERROR: below both query throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`lower(Spark SQL)`' given input columns:
        // [lower('Spark SQL')];;
        query = "SELECT lower('Spark SQL')"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT lower('abcABC123@#$%^&')"
        sparkDF = sparkSession.sql(s"$query")
        snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

    }

    test("lcase"){
        var sparkQuery = "SELECT lcase(stringcol) from sparktable"
        var snappyQuery = "SELECT lcase(stringcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "lcase_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT lcase(stringcol) from sparktable"
        snappyQuery = "SELECT lcase(stringcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "lcase_q2", "ColumnTable", pw, sparkSession)

        // ERROR: below both query throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`lower(Spark SQL)`' given input columns: [lower('Spark SQL')];;
        query = "SELECT lcase('Spark SQL')"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT lcase('abcABC123@#$%^&')"
        sparkDF = sparkSession.sql(s"$query")
        snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)
    }

    test("upper"){
        var sparkQuery = "SELECT upper(stringcol) from sparktable"
        var snappyQuery = "SELECT upper(stringcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "upper_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT upper(stringcol) from sparktable"
        snappyQuery = "SELECT upper(stringcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "upper_q2", "ColumnTable", pw, sparkSession)

        // ERROR: below both query throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`upper(Spark SQL)`' given input columns: [upper('Spark SQL')];;
        query = "SELECT upper('Spark SQL')"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT upper('abcABC123@#$%^&')"
        sparkDF = sparkSession.sql(s"$query")
        snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)
    }

    test("ucase"){
        var sparkQuery = "SELECT ucase(stringcol) from sparktable"
        var snappyQuery = "SELECT ucase(stringcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "ucase_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT ucase(stringcol) from sparktable"
        snappyQuery = "SELECT ucase(stringcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "ucase_q2", "ColumnTable", pw, sparkSession)

        // ERROR: below both query throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`upper(Spark SQL)`' given input columns: [upper('Spark SQL')];;
        query = "SELECT ucase('Spark SQL')"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT ucase('abcABC123@#$%^&')"
        sparkDF = sparkSession.sql(s"$query")
        snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

    }

    test("sort_array"){

        // throws below error
        // ERROR 42000: (SQLState=42000 Severity=20000) (Server=localhost/127.0.0.1[1528]
        // Thread=ThriftProcessor-0) Syntax error or analysis exception:
        // cannot resolve 'sort_array(array('b', 'd', 'c', 'a'), true)' due to
        // data type mismatch: Sort order in second argument requires a boolean literal.;;
        // 'Project [unresolvedalias(sort_array(array(ParamLiteral:0,1#1,b
        // , ParamLiteral:1,1#1,d, ParamLiteral:2,1#1,c,
        // ParamLiteral:3,1#1,a), ParamLiteral:4,1#4,true), None)]
        // +- OneRowRelation$
        query = "SELECT sort_array(array('b', 'd', 'c', 'a'))"
        var sparkDf = sparkSession.sql(s"$query")
        var snappyDf = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf)

        query = "SELECT sort_array(array('b', 'd', 'c', 'a'), true) as res"
        sparkDf = sparkSession.sql(s"$query")
        var snappyDf1 = snc.sql(s"$query")
        validateResult(sparkDf, snappyDf1)

        val c1s = snappyDf.columns
        val c2s = snappyDf1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("collect_list"){
        var sparkQuery = "SELECT collect_list(stringcol) from sparktable"
        var snappyQuery = "SELECT collect_list(stringcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "collect_list_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT collect_list(stringcol) from sparktable"
        snappyQuery = "SELECT collect_list(stringcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "collect_list_q2", "ColumnTable", pw, sparkSession)

        sparkQuery = "SELECT collect_list(stringcol) from sparktable"
        snappyQuery = "SELECT collect_list(stringcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "collect_list_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT collect_list(stringcol) from sparktable"
        snappyQuery = "SELECT collect_list(stringcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "collect_list_q2", "ColumnTable", pw, sparkSession)
    }

    test("collect_set"){
        var sparkQuery = "SELECT collect_set(stringcol) from sparktable"
        var snappyQuery = "SELECT collect_set(stringcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "collect_set_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT collect_set(stringcol) from sparktable"
        snappyQuery = "SELECT collect_set(stringcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "collect_set_q2", "ColumnTable", pw, sparkSession)

        sparkQuery = "SELECT collect_set(intcol) from sparktable"
        snappyQuery = "SELECT collect_set(intcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "collect_set_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT collect_set(intcol) from sparktable"
        snappyQuery = "SELECT collect_set(intcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 1,
            "collect_set_q2", "ColumnTable", pw, sparkSession)

    }

    test("concat"){
        var sparkQuery = "SELECT concat(stringcol,intcol) from sparktable"
        var snappyQuery = "SELECT concat(stringcol,intcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "concat_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT concat(stringcol,intcol) from sparktable"
        snappyQuery = "SELECT concat(stringcol,intcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "concat_q2", "ColumnTable", pw, sparkSession)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`concat(Spark, SQL)`' given input columns: [concat('Spark', 'SQL')];;
        query = "SELECT concat('Spark', 'SQL')"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT concat('Spark', 123)"
        sparkDF = sparkSession.sql(s"$query")
        var snappyDF1 = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF1)

        val c1s = snappyDF.columns
        val c2s = snappyDF1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("concat_ws"){
        var sparkQuery = "SELECT concat_ws(' ',stringcol,intcol) from sparktable"
        var snappyQuery = "SELECT concat_ws(' ',stringcol,intcol) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "concat_ws_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT concat_ws(' ',stringcol,intcol) from sparktable"
        snappyQuery = "SELECT concat_ws(' ',stringcol,intcol) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "concat_ws_q2", "ColumnTable", pw, sparkSession)

        // ERROR: org.apache.spark.sql.AnalysisException: cannot resolve
        // '`concat_ws( , Spark, SQL)`' given input columns:
        // [concat_ws(' ', 'Spark', 'SQL')];;
        query = "SELECT concat_ws(' ','Spark', 'SQL')"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT concat_ws(' ','Spark', 123)"
        sparkDF = sparkSession.sql(s"$query")
        var snappyDF1 = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF1)

        val c1s = snappyDF.columns
        val c2s = snappyDF1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("elt"){
        // ERROR : below both queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`elt(1, Spark, sql)`' given input columns:
        // [elt(1, 'Spark', 'sql')];;
        query = "SELECT elt(1,'Spark','sql')"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT elt(2,'Spark', 123)"
        sparkDF = sparkSession.sql(s"$query")
        var snappyDF1 = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF1)

        val c1s = snappyDF.columns
        val c2s = snappyDF1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("find_in_set"){
        // ERROR : below both queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`find_in_set(c, abc,b,ab,c,def)`' given input columns:
        // [find_in_set('c', 'abc,b,ab,c,def')];;
        query = "SELECT find_in_set('c','abc,b,ab,c,def')"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT find_in_set(1, '2,3,1')"
        sparkDF = sparkSession.sql(s"$query")
        var snappyDF1 = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF1)

        val c1s = snappyDF.columns
        val c2s = snappyDF1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("format_number"){
        // ERROR : below both queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`format_number(12332.123456, 4)`' given input columns:
        // [format_number(12332.123456, 4)];;
        query = "SELECT format_number(12332.123456, 4)"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT format_number(12332.123456, 1)"
        sparkDF = sparkSession.sql(s"$query")
        var snappyDF1 = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF1)

        val c1s = snappyDF.columns
        val c2s = snappyDF1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("format_string"){
        // ERROR : below both queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`format_string(Hello World %d %s, 100, days)`'
        // given input columns: [format_string('Hello World %d %s', 100, 'days')];;
        query = "SELECT format_string('Hello World %d %s', 100, 'days')"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT format_string('Hello World %d', 10)"
        sparkDF = sparkSession.sql(s"$query")
        var snappyDF1 = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF1)

        val c1s = snappyDF.columns
        val c2s = snappyDF1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("initcap"){
        // ERROR : below both queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`instr(SparkSQL, SQL)`' given input columns:
        // [instr('SparkSQL', 'SQL')];;
        query = "SELECT initcap('sPark sql')"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT initcap('ssssPark sql')"
        sparkDF = sparkSession.sql(s"$query")
        var snappyDF1 = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF1)

        val c1s = snappyDF.columns
        val c2s = snappyDF1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("instr"){
        // ERROR : below both queries throws error
        query = "SELECT instr('SparkSQL', 'SQL')"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT instr('123abcABC', 'ab')"
        sparkDF = sparkSession.sql(s"$query")
        var snappyDF1 = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF1)

        val c1s = snappyDF.columns
        val c2s = snappyDF1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("levenshtein"){
        // ERROR : below both queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`levenshtein(kitten, sitting)`' given input columns:
        // [levenshtein('kitten', 'sitting')];;
        query = "SELECT levenshtein('kitten', 'sitting')"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT levenshtein('Snappy', 'Spark')"
        sparkDF = sparkSession.sql(s"$query")
        var snappyDF1 = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF1)

        val c1s = snappyDF.columns
        val c2s = snappyDF1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("locate"){
        // ERROR : below both queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`locate(bar, foobarbar, 5)`' given input columns:
        // [locate('bar', 'foobarbar', 5)];;
        query = "SELECT locate('bar', 'foobarbar', 5)"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT locate('abc', 'defghrih', 2)"
        sparkDF = sparkSession.sql(s"$query")
        var snappyDF1 = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF1)

        val c1s = snappyDF.columns
        val c2s = snappyDF1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("lpad"){
        // ERROR : below both queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`lpad(hi, 5, ??)`' given input columns: [lpad('hi', 5, '??')];;
        query = "SELECT lpad('hi', 5, '??')"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT lpad('hi', 1, '??')"
        sparkDF = sparkSession.sql(s"$query")
        var snappyDF1 = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF1)

        val c1s = snappyDF.columns
        val c2s = snappyDF1.columns
        assert(!c1s.sameElements(c2s))
    }

    test("add_months"){
        var sparkQuery = "SELECT add_months(datecol,1) from sparktable"
        var snappyQuery = "SELECT add_months(datecol,1) from rowtable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "concat_ws_q1", "RowTable", pw, sparkSession)

        sparkQuery = "SELECT add_months(datecol,1) from sparktable"
        snappyQuery = "SELECT add_months(datecol,1) from columnTable"
        assertQueryFullResultSet(snc, sparkQuery, snappyQuery, 2,
            "concat_ws_q2", "ColumnTable", pw, sparkSession)

        // ERROR : below both queries throws error
        // org.apache.spark.sql.AnalysisException: cannot resolve
        // '`add_months(CAST(2016-08-31 AS DATE), 1)`' given input columns:
        // [add_months(CAST('2016-08-31' AS DATE), 1)];;
        query = "SELECT add_months('2016-08-31', 1)"
        var sparkDF = sparkSession.sql(s"$query")
        var snappyDF = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF)

        query = "SELECT add_months('2016-08-31', 0)"
        sparkDF = sparkSession.sql(s"$query")
        var snappyDF1 = snc.sql(s"$query")
        validateResult(sparkDF, snappyDF1)

        val c1s = snappyDF.columns
        val c2s = snappyDF1.columns
        assert(!c1s.sameElements(c2s))
    }


}