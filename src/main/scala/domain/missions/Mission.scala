package domain.missions

import domain.Team

import scala.util.Try

case class Mission(tasks: List[Task]) {
    def attempt(team: Team): Try[Team] =
        tasks.foldLeft(Try(team))((teamTry, task) => teamTry.flatMap(team => team.perform(task)))
}