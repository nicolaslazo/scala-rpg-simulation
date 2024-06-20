package domain

import scala.collection.immutable.HashMap

type StatBlock = HashMap[Stat, Int]

object StatBlock {
    val empty: StatBlock = apply()

    def apply(): StatBlock = HashMap.empty[Stat, Int]

    def apply(entries: (Stat, Int)*): StatBlock = HashMap(entries: _*)
}

extension (statBlock: StatBlock) {
    def getStat(stat: Stat): Int = statBlock.getOrElse(stat, 0)

    def applyModifiers(modifiers: StatBlock): StatBlock =
        statBlock.merged(modifiers) { case ((k, v1), (_, v2)) => (k, (v1 + v2).max(0)) }

    def applyEffect(effect: (StatBlock, Hero) => StatBlock, context: Hero): StatBlock = effect(statBlock, context)

    def applyEffects(effects: Iterable[(StatBlock, Hero) => StatBlock], context: Hero): StatBlock =
        effects.foldLeft(statBlock)((stats, effect) => stats.applyEffect(effect, context))
}