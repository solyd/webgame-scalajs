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

object websocketdemo {
  def mainsss(args: Array[String]): Unit = {
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


        val fontHeight= 12
        c.font = s"${fontHeight}px Source Code Pro"
        c.fillStyle = "#000000"
        c.fillText(s"fps: $fpsToDisplay", canvas.width - 100, fontHeight)

        tsLast = ts
        dom.window.requestAnimationFrame(ts => render(ts))
      }
    }

    (new GameLoop).render(dom.window.performance.now())
  }

}
