package domain

import scala.collection.immutable.HashMap

type StatBlock = HashMap[Stat, Int]

object StatBlock {
    val empty: StatBlock = apply()

    def apply(): StatBlock = HashMap.empty[Stat, Int]

    // TODO: Terminar de entender esta sintaxis
    def apply(entries: (Stat, Int)*): StatBlock = HashMap(entries: _*)
}

// TODO: realmente necesito tener un singleton object Y métodos de extensión para conseguir todo lo que necesito?
extension (statBlock: StatBlock) {
    def getStat(stat: Stat): Int = statBlock.getOrElse(stat, 0)

    def applyModifiers(modifiers: StatBlock): StatBlock =
        statBlock.merged(modifiers) { case ((k, v1), (_, v2)) => (k, (v1 + v2).max(0)) }

    def applyEffect(effect: (StatBlock, Hero) => StatBlock, context: Hero): StatBlock = effect(statBlock, context)

    // TODO: Siento que esto se podría simplificar pero estoy cansado
    def applyEffects(effects: Iterable[(StatBlock, Hero) => StatBlock], context: Hero): StatBlock =
        effects.foldLeft(statBlock)((stats, effect) => stats.applyEffect(effect, context))
}