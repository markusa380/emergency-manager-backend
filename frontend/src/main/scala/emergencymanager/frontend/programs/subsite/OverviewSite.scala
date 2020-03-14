package emergencymanager.frontend.programs.subsite

import emergencymanager.frontend.Client
import emergencymanager.frontend.Dom._

import cats.syntax._
import cats.implicits._
import cats.effect._

import outwatch._
import outwatch.dsl._
import outwatch.reactive.handler._
import colibri._

case class OverviewSite(
    dom: VNode,
    onCreate: Observable[Unit],
    onEdit: Observable[String]
)

object OverviewSite {

    def create(
        implicit ctx: ContextShift[IO]
    ): SyncIO[OverviewSite] = for {
        createHandler <- Handler.create[Unit]
        editHandler <- Handler.create[String]
        reloadHandler <- Handler.create[Unit]
    } yield {

        val supplies = Observable
            .merge(
                reloadHandler
            )
            .startWith(List(()))
            .concatMapAsync(_ => Client.loadSupplies)

        val dom = container(
            h1("Emergency Supplies Manager"),
            table(
                cls := "table",
                styles.width := "100%",
                thead(
                    tr(
                        th("", attr("scope") := "col"),
                        th("Name", attr("scope") := "col"),
                        th("Best Before Date", attr("scope") := "col"),
                        th("Calories / 100g", attr("scope") := "col"),
                        th("Weight (g)", attr("scope") := "col"),
                        th("#", attr("scope") := "col")
                    )
                ),
                tbody(
                    supplies.map(list =>
                        list.map { s =>
                            val bbd = s.bestBefore
                                .map(_.toString)
                                .getOrElse("")

                            tr(
                                td(primaryButton("Edit"), onClick.use(s.id) --> editHandler),
                                td(s.name),
                                td(bbd),
                                td(s.kiloCalories.toString + " kcal"),
                                td(s.weightGrams.toString + " g"),
                                td(s.number)
                            )
                        }
                    )
                )
            )
        )

        OverviewSite(dom, createHandler, editHandler)
    }
}