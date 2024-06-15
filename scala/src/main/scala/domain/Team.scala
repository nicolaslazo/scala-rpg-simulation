package domain

case class Team(name: String, earnings: Int = 0, members: Set[Hero] = Set()) {
    val bestMember: (Hero => Int) => Option[Hero] = members.maxByOption

    //    def getItem(item: Item): Team {
    //        val mainStats:
    //    }
}