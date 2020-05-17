import java.time.{LocalDate, LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}
import java.util.Random
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

import org.scalajs.dom
import org.scalajs.dom.{Element, Event, KeyboardEvent, document, html}

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

object gameoflife {
  def main(args: Array[String]): Unit = {
    println(s"--- loaded @ ${LocalDateTime.now(ZoneOffset.UTC)} UTC ---")
    println(s"args: ${args.mkString(",")}")

    val canvas = dom.document.getElementById("canvas").asInstanceOf[html.Canvas]
    val c = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    canvas.width = canvas.parentElement.clientWidth
    canvas.height = canvas.parentElement.clientHeight

    println(s"canvas.width: ${canvas.width}, canvas.height: ${canvas.height}")

    class Grid()

    class GameLoop {
      var tsLast: Double = 0
      var fpsCalcLast: Double = 0

      val calcFpsInterval = Duration(100, TimeUnit.MILLISECONDS)

      var frameTimes = mutable.ArrayBuffer.empty[Double]
      val FpsLimit = 60
      frameTimes.sizeHint((calcFpsInterval.toSeconds * FpsLimit).toInt)

      var avgFrameTimeToDispaly: Double = 0
      var fpsToDisplay: Double = 0

      dom.document.onkeydown = (e: dom.KeyboardEvent) => {
        println("down", e.key)
      }

      dom.document.onkeypress = (e: dom.KeyboardEvent) => {
        println("press", e.key)
      }

      dom.document.onkeyup = (e: dom.KeyboardEvent) => println("up", e.key)


      val gridUpdateInterval = Duration(1, TimeUnit.SECONDS)
      var gridLastUpdate: Double = 0
      var gridGeneration = 0

      val size = 10

      val rnd = new Random(214310)

      var grid = Array.ofDim[Boolean](
        math.min(canvas.width, 10000) / size,
        math.min(canvas.height, 10000) / size
      )

      var gridplusone = grid.map(_.clone)

//      var gridplusone = Array.ofDim[Boolean](
//        math.min(canvas.width, 400) / size,
//        math.min(canvas.height, 400) / size
//      )

      println("grid size", grid.size, grid(0).size)

      // gen live cells
      for (_ <- 0 to 500) {
        grid(rnd.nextInt(grid.size))(rnd.nextInt(grid(0).size)) = true
      }

      def add(x: Int)(v: Int) = x+v

      def neighbours(pos: (Int, Int)) = Seq(
        (pos._1 + 1, pos._2 + 1),
        (pos._1 + 1, pos._2 + 0),
        (pos._1 + 1, pos._2 - 1),
        (pos._1 + 0, pos._2 + 1),
        (pos._1 + 0, pos._2 - 1),
        (pos._1 - 1, pos._2 + 1),
        (pos._1 - 1, pos._2 + 0),
        (pos._1 - 1, pos._2 - 1),
      ).filter( p => p != pos && p._1 >= 0 && p._2 >= 0 && p._1 < grid.size && p._2 < grid(0).size)

      def render(ts: Double): Unit = {
        frameTimes.append(ts - tsLast)
        tsLast = ts

        if (ts - fpsCalcLast > calcFpsInterval.toMillis) {
          avgFrameTimeToDispaly = frameTimes.sum / frameTimes.size
          fpsToDisplay = frameTimes.size / ((ts - fpsCalcLast) / 1000.0)
          fpsCalcLast = ts
          frameTimes.clear
        }

        // clear screen
        c.clearRect(0, 0, canvas.width, canvas.height)

        // bg color
        c.fillStyle = "#f8f8f8"
        c.fillRect(0, 0, canvas.width, canvas.height)

        if (ts - gridLastUpdate > gridUpdateInterval.toMillis) {
          gridLastUpdate = ts
          gridGeneration += 1

          println(s"calculating generation #${gridGeneration} @ $ts")

          for {
            i <- 0 until grid.size
            j <- 0 until grid(0).size
          } {
            //println(s"proc cell $i $j")

            val aliveNeighbours =
              (if ((i + 1 < grid.size) && (j + 1 < grid(0).size) && grid(i + 1)(j + 1)) 1 else 0) +
                (if ((i + 1 < grid.size) && (j + 0 < grid(0).size) && grid(i + 1)(j + 0)) 1 else 0) +
                (if ((i + 1 < grid.size) && (j - 1 >= 0) && grid(i + 1)(j - 1)) 1 else 0) +
                (if ((i + 0 < grid.size) && (j + 1 < grid(0).size) && grid(i + 0)(j + 1)) 1 else 0) +
                (if ((i + 0 < grid.size) && (j - 1 >= 0) && grid(i + 0)(j - 1)) 1 else 0) +
                (if ((i - 1 >= 0) && (j + 1 < grid(0).size) && grid(i - 1)(j + 1)) 1 else 0) +
                (if ((i - 1 >= 0) && (j + 0 < grid(0).size) && grid(i - 1)(j + 0)) 1 else 0) +
                (if ((i - 1 >= 0) && (j - 1 >= 0) && grid(i - 1)(j - 1)) 1 else 0)

//            val aliveNeighbours = neighbours(i, j)
//              .map { case (ni, nj) => if (grid(ni)(nj)) 1 else 0 }
//              .sum

            // cell is alive, check if it continue to live on
            if (grid(i)(j)) {
              gridplusone(i)(j) = aliveNeighbours >= 2 && aliveNeighbours <= 3
            } else {
              gridplusone(i)(j) = aliveNeighbours == 3
            }

            if (gridplusone(i)(j))  {
              c.fillStyle = "#000000"
              c.fillRect(i * size, j * size, size, size)
            }
          }

          grid = gridplusone
        }

        for {
          i <- 0 until grid.size
          j <- 0 until grid(0).size
        } {
          if (grid(i)(j))  {
            c.fillStyle = "#000000"
            c.fillRect(i * size, j * size, size, size)
          }
        }

        val fontHeight= 12
        c.font = s"${fontHeight}px Source Code Pro"
        c.fillStyle = "#000000"
        c.fillText(s"fps: $fpsToDisplay", canvas.width - 100, fontHeight)
        c.fillText(s"generation: $gridGeneration", canvas.width - 200, fontHeight * 2)

        tsLast = ts
        dom.window.requestAnimationFrame(ts => render(ts))
      }
    }

    (new GameLoop).render(dom.window.performance.now())
  }

}
