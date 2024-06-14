package domain

case class Team(name: String, earnings: Int = 0, members: List[Hero] = List()) {
    val bestMember: (Hero => Int) => Option[Hero] = members.maxByOption

//    def getItem(item: Item): Team {
//        val mainStats:
//    }
}