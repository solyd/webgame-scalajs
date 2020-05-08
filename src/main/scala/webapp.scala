import java.time.{LocalDate, LocalDateTime, ZoneId, ZoneOffset, ZonedDateTime}
import java.util.concurrent.TimeUnit

import org.scalajs.dom
import org.scalajs.dom.{KeyboardEvent, document, html}

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

object webapp {
  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    parNode.textContent = text
    targetNode.appendChild(parNode)
  }

  @JSExportTopLevel("addClickedMessage")
  def addClickedMessage(): Unit = {
    appendPar(document.body, "You clicked the button!")
  }

  var mvfact = 1

  @JSExportTopLevel("mvfactChanged")
  def mvfactChanged(v: String): Unit = {
    val vi = v.toInt
    document.getElementById("mvfacttxt").textContent = s"mvfact: $vi"
    mvfact = vi
  }

  def main(args: Array[String]): Unit = {
    println(s"--- loaded @ ${LocalDateTime.now(ZoneOffset.UTC)} UTC ---")
    println(s"args: ${args.mkString(",")}")

    val canvas = dom.document.getElementById("canvas").asInstanceOf[html.Canvas]
    val c = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

//    val ccontainer = document.getElementById("canvas_container")
//    ccontainer.setAttribute(
//      "style",
//      s"width:${dom.window.innerWidth}, height:${dom.window.innerHeight}," +
//        "padding: 0px; margin: 0px; border:1px solid red"
//    )

    canvas.width = canvas.parentElement.clientWidth
    canvas.height = canvas.parentElement.clientHeight * 4
//    canvas.width = 600
//    canvas.height = 800

    //dom.window.onresize

    println(c)
    println(s"canvas.width: ${canvas.width}, canvas.height: ${canvas.height}")

    c.fillStyle = "#fAfAfA"
    c.fillRect(0, 0, canvas.width, canvas.height)

    c.fillStyle = "black"
    var down = false
    canvas.onmousedown = (e: dom.MouseEvent) => down = true
    canvas.onmouseup = (e: dom.MouseEvent) => down = false

    canvas.onmousemove = {
      (e: dom.MouseEvent) =>
        val rect = canvas.getBoundingClientRect()
        println(rect.left, rect.top, e.clientX, e.clientY)
        if (down) c.fillRect(
          e.clientX - rect.left,
          e.clientY - rect.top,
          10, 10
        )
    }

    // TODO difference?
//    canvas.onkeydown
//    dom.window.onkeydown

    
    val gradient = c.createLinearGradient(
      canvas.width / 2 - 100, 0, canvas.width/ 2 + 100, 0
    )
    gradient.addColorStop(0,"red")
    gradient.addColorStop(0.5,"green")
    gradient.addColorStop(1,"blue")

    c.textAlign = "start"
    c.textBaseline = "middle"

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
        e.key.toLowerCase match {
          case "escape" => player.reset()
          case _ => ()
        }
      }

      dom.document.onkeypress = (e: dom.KeyboardEvent) => {
        println("press", e.key)
        e.key.toLowerCase match {
          case "w" => player.move(( 0, -1))
          case "a" => player.move((-1,  0))
          case "s" => player.move(( 0, +1))
          case "d" => player.move((+1,  0))

          case _ => ()
        }
      }

      dom.document.onkeyup = (e: dom.KeyboardEvent) => println("up", e.key)

      class Player {
        var pos: (Int, Int) = _
        var dims: (Int, Int) = _
        var color: String = _

        def reset(): Unit = {
          pos = (20, 20)
          dims = (10, 10)
          color = "#FF00FF"
        }

        def move(delta: (Int, Int)): Unit = pos = (pos._1 + mvfact * delta._1, pos._2 + mvfact * delta._2)

        def render(ts: Double): Unit = {
          val savedstyle = c.fillStyle
          c.fillStyle = color
          c.fillRect(pos._1, pos._2, dims._1, dims._2)
          c.fillStyle = savedstyle
        }

        reset()
      }

      val player = new Player

      def render(ts: Double): Unit = {
        frameTimes.append(ts - tsLast)
        tsLast = ts

        if (ts - fpsCalcLast > calcFpsInterval.toMillis) {
          avgFrameTimeToDispaly = frameTimes.sum / frameTimes.size
          fpsToDisplay = frameTimes.size / ((ts - fpsCalcLast) / 1000.0)
          fpsCalcLast = ts
          frameTimes.clear
        }

        c.clearRect(
          0, 0, canvas.width, canvas.height
        )

        c.fillStyle = "#f8f8f8"
        c.fillRect(0, 0, canvas.width, canvas.height)

        player.render(ts)

        val fontHeight= 12
        c.font = s"${fontHeight}px Source Code Pro"
        c.fillStyle = "#000000"
        c.fillText(s"fps: $fpsToDisplay", 0, fontHeight)
        //c.fillText(s"avg frame time: $avgFrameTimeToDispaly", 0, 2 * fontHeight)

//        val date = new js.Date()
//        c.font = "75px sans-serif"
//        c.fillStyle = gradient
//        c.fillText(
//          Seq(
//            date.getHours(),
//            date.getMinutes(),
//            date.getSeconds()
//          ).mkString(":"),
//          canvas.width / 2,
//          canvas.height / 2
//        )

        tsLast = ts
        dom.window.requestAnimationFrame(ts => render(ts))
      }
    }

    /*code*/

    (new GameLoop).render(dom.window.performance.now())




    /// ---------------------------------------------------------------------------
//    appendPar(document.body, "WOWOWOW")
//
//    document.addEventListener("DOMContentLoaded", { (e: dom.Event) =>
//      println("DOMContentLoaded")
//    })
//
//    val button = document.createElement("button")
//    button.textContent = "Click me!"
//    button.addEventListener("click", { (e: dom.MouseEvent) =>
//      addClickedMessage()
//    })
//    document.body.appendChild(button)
  }
}
