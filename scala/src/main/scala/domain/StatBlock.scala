package domain

case class StatBlock(health: Int = 0, strength: Int = 0, speed: Int = 0, intelligence: Int = 0) {
    def +(that: StatBlock): StatBlock = StatBlock(this.health + that.health,
        this.strength + that.strength,
        this.speed + that.speed,
        this.intelligence + that.intelligence)
}
