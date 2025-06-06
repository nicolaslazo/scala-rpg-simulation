# Scala RPG Simulation

[![🇦🇷 Leer en Español](https://img.shields.io/badge/🇦🇷-Leer%20en%20español-blue)](README.es.md)

This was the second of the two assignments for the Advanced Programming Techniques course at the National Technological University of Buenos Aires, first term of 2024.

This was meant to be a group project, but by that time in the semester, my teammates had dropped out of the class. I completed it with close supervision from one of the PAs.

I found this project to be profoundly enjoyable. From the start, I challenged myself to write it entirely without declaring a single mutable variable. The implementation went through multiple iterations, through which I could witness my codebase get increasingly simpler. I also took to Scala's monadic way of thinking, which would prepare me to later adopt Rust's type system without much struggle.

The original assignment is transcribed below, translated into English.

---

## Group Project - Functional - 1C2024

### Introduction

We will be modeling a role-playing game where heroes form teams to undertake various
missions. Our goal is to determine the outcome of sending a team on a mission, to
avoid sending them to a certain death.

**IMPORTANT:** This project must be implemented in a way that applies the principles
of the hybrid object-functional paradigm taught in class. It is not enough to make
the code work in objects; functional tools must be leveraged, design decisions must
be justified, and the appropriate paradigm must be chosen for each concept.

The following aspects will be considered for grading:

- Use of Immutability vs. Mutability
- Use of Parametric Polymorphism (Pattern Matching) vs. Ad-Hoc Polymorphism
- Leveraging polymorphism between objects and functions
- Proper use of functional tools
- Software quality
- Interface design and type selection

---

## General Domain Description

### Heroes

Heroes are the protagonists of the game. They carry an inventory and may or may not have a job. The main attributes of heroes are represented by numerical values called Stats.

#### Stats

HP, strength, speed, and intelligence. Each hero has an innate base value for each of these attributes, which may vary from person to person. Additionally, a hero's job and equipped items can modify their stats in various ways. Stats can never be negative; if something reduces a stat below 1, it should be considered 1 (this only applies to final stats).

#### Job

A job is a specialization that some adventurers choose to pursue. A hero’s job affects their stats and grants access to special items and activities. Each job has a primary stat that impacts its performance. A hero can only have one job at a time, but it can be changed at any moment to any other job (or none).

Some examples of jobs:

- **Warrior:** +10 HP, +15 Strength, -10 Intelligence. Primary stat: Strength.
- **Mage:** +20 Intelligence, -20 Strength. Primary stat: Intelligence.
- **Thief:** +10 Speed, -5 HP. Primary stat: Speed.

#### Inventory

To successfully complete their missions, heroes equip various tools and special armor that protect and aid them. Each hero can wear:

- A single hat or helmet on their head.
- One armor or robe on their torso.
- One weapon or shield in each hand (some weapons require both hands).
- Any number of talismans.

Each item may have specific equipping restrictions and affects the final stats of the wearer. A hero can equip an item at any time, provided they meet the requirements. If an item is equipped in a slot that is already occupied, the previous item is discarded.

Examples of items:

- **Viking Helmet:** +10 HP. Can only be used by heroes with base strength > 30. (Head slot)
- **Magic Wand:** +20 Intelligence. Usable only by mages (or thieves with base intelligence > 30). (One-handed weapon)
- **Elegant-Sport Armor:** +30 Speed, -30 HP. (Armor slot)
- **Old Bow:** +2 Strength. (Two-handed weapon)
- **Anti-Theft Shield:** +20 HP. Cannot be equipped by thieves or heroes with base strength < 20. (One-handed weapon)
- **Dedication Talisman:** All stats increase by 10% of the job's main stat.
- **Minimalism Talisman:** +50 HP, -10 HP for each additional equipped item.
- **Water Buffalo Headband:** If the hero has more strength than intelligence, +30 Intelligence; otherwise, +10 to all stats except intelligence. Can only be equipped by heroes without a job. (Head slot)
- **Cursed Talisman:** All stats become 1.
- **Life Sword:** Sets the hero's strength equal to their HP.

### Team

No one is an island. Adventurers often form teams to improve their chances of success on missions. A team is a group of heroes who work together and share mission rewards. Each team has a **common gold pool** representing their earnings and a fantasy name (e.g., *The Wild Ducks*).

### Tasks and Missions

Adventurers make a living by completing missions in exchange for treasure. Missions consist of a set of tasks that must be completed and a reward for the team upon success.

Tasks can vary greatly. Each task must be completed by a single hero from the team, who may be affected in some way by performing it.

For example:
- **"Fight a monster"** reduces the HP of any hero with strength < 20 by 10.
- **"Break down a door"** has no effect on mages or thieves, but increases strength by 1 and decreases HP by 5 for all others.
- **"Steal a talisman"** adds a talisman to the hero’s inventory.

However, not all teams can complete every task. Each task has an associated "ease" value, which represents how easy it is for a hero to complete it (positive values indicate greater ease, negative values indicate greater difficulty). The ease calculation varies by team. Some teams may be entirely incapable of performing certain tasks.

For example:
- "Fight a monster" has an ease value of 10 for any hero or 20 if the team leader is a warrior.
- "Break down a door" has an ease value equal to the hero’s intelligence + 10 for each thief in the team.
- "Steal a talisman" has an ease value equal to the hero’s speed but cannot be attempted if the team leader is not a thief.

### Rewards

Mission rewards can vary widely, including:

- Gold for the team’s common pool
- A new item
- Increased stats for certain heroes
- A new hero joining the team

---

## Requirements

The following use cases must be implemented, along with corresponding tests and documentation (including at least a class diagram):

### 1. Forging a Hero

Model heroes, items, and jobs while implementing necessary operations and validations to manipulate them consistently.

- Prevent invalid states.
- Choose the most suitable types and representations to ensure a scalable and robust hybrid object-functional model.
- Ensure heroes can:
  - Retrieve and modify their stats.
  - Equip items.
  - Change jobs.

### 2. Team Functionality

Implement teams according to the description, providing the following features:

- **Best hero according to:** Given a quantifier `[Hero => Int]`, find the team member with the highest value.
- **Obtain an item:** If an item is acquired, it is given to the hero who benefits most from it in their job’s main stat. Otherwise, it is sold for gold.
- **Add a member:** Allows a new hero to join the team.
- **Replace a member:** Swaps a hero in the team for another.
- **Leader:** The hero with the highest value in their job’s main stat. If tied, the team has no clear leader.

### 3. Missions

Model missions and allow adventurer teams to attempt them. For each mission, the team must try to complete every task in sequence.

Each individual task must be performed by a single hero—the one with the highest ease for that task. When a task is performed, its effects are applied to the hero immediately (i.e., before proceeding to the next task).

If no hero is able to complete a task, the mission is considered a failure. All effects from previously completed tasks are undone, and the system must report the team's state along with the task that could not be completed.

If the mission is successful, the team receives the mission's reward and the final state of the team is reported. Only successfully completed missions grant their rewards.

### 4. The Tavern

A notice board contains a set of available missions. Implement the following features:

- **Choose Mission:** Select the best mission for a team, according to a given comparison criterion `((Team, Team) => Boolean)`. This function receives the resulting team states after attempting two missions and returns `true` if the first result is better than the second.  
  For example, if the criterion is `{ (t1, t2) => t1.gold > t2.gold }`, the mission that would earn the team the most gold should be chosen.  
  Importantly, choosing a mission must not alter the team's state in any way. Also, consider that the team may not be able to complete any mission.

- **Training:** When a team trains, it attempts to complete all missions, one after another, always choosing the best available mission to attempt next (according to the given criterion). Each mission is attempted after collecting the reward from the previous one, and the process continues until all missions are completed or the team fails a mission.
