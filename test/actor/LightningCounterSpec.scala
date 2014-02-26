package actor

import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import actors.LightningCounter.CurrentStrikes
import actors.LightningCounter.RegisterStrike
import actors.LightningCounter.props
import actors.Notifier._
import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import akka.testkit.TestProbe
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class LightningCounterSpec extends TestKit(ActorSystem("LightningCounter"))
    with FunSuite
    with BeforeAndAfterAll
    with ShouldMatchers
    with ImplicitSender {
  val testClientActor = TestProbe()
  val testNotifierActor = TestProbe()

  override def afterAll(): Unit = {
    system.shutdown()
  }

  test("LightningCounter should respond with current registered strikes") {
    // Set up
    val instance = system.actorOf(props(3, 2.seconds, testNotifierActor.ref))

    // Test
    testClientActor.send(instance, RegisterStrike(0L))
    testClientActor.send(instance, RegisterStrike(1L))

    // Verify
    testClientActor.expectMsg(CurrentStrikes(Seq(0L)))
    testClientActor.expectMsg(CurrentStrikes(Seq(1L, 0L)))
    testNotifierActor.expectNoMsg()
  }

  test("LightningCounter should exclude expired strikes in response") {
    // Set up
    val instance = system.actorOf(props(3, 2.seconds, testNotifierActor.ref))

    // Test
    testClientActor.send(instance, RegisterStrike(0L))
    testClientActor.send(instance, RegisterStrike(2001L))

    // Verify
    testClientActor.expectMsg(CurrentStrikes(Seq(0L)))
    testClientActor.expectMsg(CurrentStrikes(Seq(2001L)))
    testNotifierActor.expectNoMsg()
  }

  test("LightningCounter should send notification when strikes drop below safety threshold") {
    // Set up
    val instance = system.actorOf(props(3, 2.seconds, testNotifierActor.ref))

    // Test
    testClientActor.send(instance, RegisterStrike(0L))
    testClientActor.send(instance, RegisterStrike(500L))
    testClientActor.send(instance, RegisterStrike(1000L))
    testClientActor.send(instance, RegisterStrike(1500L))

    // Verify
    testClientActor.expectMsg(CurrentStrikes(Seq(0L)))
    testClientActor.expectMsg(CurrentStrikes(Seq(500L, 0L)))
    testClientActor.expectMsg(CurrentStrikes(Seq(1000L, 500L, 0L)))
    testClientActor.expectMsg(CurrentStrikes(Seq(1500L, 1000L, 500L, 0L)))
    testNotifierActor.within(2001.millis, 2499.millis) {
      testNotifierActor.expectMsg(Notify)
    }
  }
}