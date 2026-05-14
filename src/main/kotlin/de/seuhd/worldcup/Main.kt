package de.seuhd.worldcup

fun main() {
    val data = JsonLoader.loadJson()
    val teamsById: Map<String, Team> = data.groups.flatMap { it.teams }.associateBy { it.id }

    while (true) {
        printMenu()
        when (Console.readInt("Choose an option (1 to 7): ", (1..7).toSet())) {
            1 -> showStandings(data.groups)
            2 -> showMatches(data.groups, teamsById)
            3 -> placeBets(data.groups, teamsById)
            4 -> removeBets(data.groups)
            5 -> changeBets(data.groups)
            6 -> showBettingScore(data.groups)
            7 -> {
                println("Bye!")
                return
            }
        }
    }
}

private fun printMenu() {
    println()
    println("===== FIFA World Cup 2026 - Betting Console =====")
    println("1) Show Standings")
    println("2) Show Matches")
    println("3) Place Bets")
    println("4) Remove Bets")
    println("5) Change Bets")
    println("6) Show Betting Score")
    println("7) Exit")
    println("=================================================")
}

private fun showStandings(allGroups: List<Group>) {
    println("\n--- Standings ---")
    println("Available groups: ${allGroups.joinToString { it.name }}")
    println("Enter a group name (e.g. \"Group A\") or press Enter to see ALL groups:")
    val input = Console.readLineOrEmpty().trim()

    val groups = if (input.isEmpty()) {
        allGroups
    } else {
        val match = allGroups.find { it.name.equals(input, ignoreCase = true) }
        if (match == null) {
            println("No group named \"$input\".")
            return
        }
        listOf(match)
    }
    groups.forEach(::printGroupTable)
    Console.waitForEnter()
}

private fun printGroupTable(group: Group) {
    println("\n${group.name}")
    println(formatRow("Pos", "Team", "P", "GF", "GA", "GD"))
    StandingsService.calculate(group).forEachIndexed { idx, entry ->
        println(formatRow(idx + 1, entry.team.name, entry.points, entry.goalsFor, entry.goalsAgainst, entry.goalDiff))
    }
}

private fun formatRow(pos: Any, team: Any, p: Any, gf: Any, ga: Any, gd: Any): String =
    "%-3s %-20s %3s %3s %3s %3s".format(pos, team, p, gf, ga, gd)

private fun showMatches(allGroups: List<Group>, teamsById: Map<String, Team>) {
    println("\n--- Show Matches ---")
    val group = chooseGroup(allGroups) ?: return

    println("\nMatches for ${group.name}:")
    group.matches.forEach { match ->
        val score = match.scoreOrNull()?.let { (h, a) -> "$h:$a" } ?: "vs"
        val home = teamsById.nameOf(match.homeTeam)
        val away = teamsById.nameOf(match.awayTeam)
        println("${match.date}: $home $score $away")
    }
    Console.waitForEnter()
}

private fun placeBets(allGroups: List<Group>, teamsById: Map<String, Team>) {
    println("\n--- Place Bets ---")
    val group = chooseGroup(allGroups) ?: return

    println("You are about to bet on every match in ${group.name}.")
    group.matches.forEach { match ->
        val home = teamsById.nameOf(match.homeTeam)
        val away = teamsById.nameOf(match.awayTeam)
        println("\n$home vs $away on ${match.date}")
        val code = Console.readInt(
            "Your prediction (0 = Draw, 1 = Home win, 2 = Away win, 3 = Predict Home and Away Score): ",
            setOf(0, 1, 2, 3)
        )

        val bet = if (code == 3) {
            val homeScore = Console.readInt("Predict Home Score: ")
            val awayScore = Console.readInt("Predict Away Score: ")

            Bet(
                matchId = match.matchId,
                prediction = Prediction.outcomeOf(homeScore, awayScore),
                predictedHomeScore = homeScore,
                predictedAwayScore = awayScore
            )
        } else {
            Bet(
                matchId = match.matchId,
                prediction = Prediction.fromCode(code)
            )
        }
        BettingService.placeBet(bet)
    }
    println("\nAll bets for ${group.name} stored.")
    Console.waitForEnter()
}

private fun showBets(matches: List<Match>) {
    var hasBet = false

    matches.forEach { match ->
        val bet = BettingService.showBet(match.matchId)

        if (bet != null) {
            println("${match.homeTeam} vs ${match.awayTeam} = $bet")
            hasBet = true
        }
    }

    if (!hasBet) {
        println("No bets found!!")
        return
    }
}

private fun removeBets(allGroups: List<Group>) {
    println("\n From which group do you want to remove the bet? Enter the group name (eg. Group A): ")

    val group = chooseGroup(allGroups = allGroups) ?: return
    val matches: List<Match> = group.matches

    showBets(matches)

    println("\n Which bet do you want to remove from ${group.name}? Enter the matchId: ")

    val matchId = Console.readInt("Match ID: ")
    BettingService.removeBet(matchId)
    Console.waitForEnter()
}

private fun changeBets(allGroups: List<Group>) {
    println("\n From which group do you want to change the bet? Enter the group name (eg. Group A): ")

    val group = chooseGroup(allGroups) ?: return
    val matches = group.matches

    showBets(matches)

    println("\n Which bet do you want to change from ${group.name}? Enter the matchId: ")

    val matchId = Console.readInt("Match ID: ")
    val bet = BettingService.showBet(matchId) ?: throw IllegalArgumentException("No bet found for match Id ${matchId}!")
    BettingService.changeBet(bet)
    Console.waitForEnter()
}

private fun showBettingScore(allGroups: List<Group>) {
    println("\n--- Betting Score ---")
    val allMatches = allGroups.flatMap { it.matches }
    val result = BettingService.evaluate(allMatches)
    println(
        "You have ${result.correct} correct prediction(s) and ${result.incorrect} incorrect, " +
            "out of ${result.evaluated} evaluated match(es)."
    )

    BettingService.evaluateBonus(allMatches)
    Console.waitForEnter()
}

private fun chooseGroup(allGroups: List<Group>): Group? {
    println("Available groups: ${allGroups.joinToString { it.name }}")
    val answer = Console.readLineOrEmpty().trim()
    return allGroups.find { it.name.equals(answer, ignoreCase = true) }
        ?: run { println("No such group, returning to main menu."); null }
}

private fun Map<String, Team>.nameOf(teamId: String): String = this[teamId]?.name ?: teamId
