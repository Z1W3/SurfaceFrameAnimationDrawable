package catt.sample

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
//        var list:ArrayList<AA> = ArrayList()
//        list.add(AA(1, Any()))
//        list.add(AA(19, Any()))
//        list.add(AA(2, Any()))
//        list.add(AA(3, Any()))
//        list.add(AA(4, Any()))
//        list.add(AA(50, Any()))
//        list.add(AA(6, Any()))
//        list.add(AA(7, Any()))
//        list.sortDescending()
//        list.forEach{
//            println(it.id)
//        }

        var a = 3

        println(a > 0)

    }



    class AA(val id:Int, val any:Any) : Comparable<AA>{

        override fun compareTo(other: AA): Int = this.id.compareTo(other.id)

//        override fun compare(o1: AA, o2: AA): Int {
//           return when{
//               o1.id > o2.id -> 1
//               o1.id < o2.id -> -1
//               else -> 0
//           }
//        }

    }
}
