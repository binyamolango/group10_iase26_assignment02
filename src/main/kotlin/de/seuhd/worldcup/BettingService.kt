package de.seuhd.worldcup

/**
 * Possible outcomes of a match. The numeric codes are the ones the user types into
 * the menu (1 = home win, 2 = away win, 0 = draw).
 */
enum class Prediction(val code: Int) {
    DRAW(0),
    HOME_WIN(1),
    AWAY_WIN(2);

    companion object {
        fun fromCode(code: Int): Prediction =
            entries.firstOrNull { it.code == code }
                ?: error("Invalid prediction code: $code (allowed: 0, 1, 2)")

        /** Outcome implied by a final score. */
        fun outcomeOf(homeScore: Int, awayScore: Int): Prediction = when {
            homeScore > awayScore -> HOME_WIN
            homeScore < awayScore -> AWAY_WIN
            else -> DRAW
        }
    }
}

/** A user prediction for a single match. Optionally includes an exact score for bonus evaluation. */
data class Bet(
    val matchId: Int,
    val prediction: Prediction,
    val predictedHomeScore: Int? = null,
    val predictedAwayScore: Int? = null
)

/** Outcome of comparing the user's bets against the actual results. */
data class BettingResult(
    val correct: Int,
    val evaluated: Int
) {
    val incorrect: Int get() = evaluated - correct
}

/**
 * In-memory bet store. One bet per match id; placing a new bet for the same match
 * replaces the previous one.
 */
object BettingService {

    private val bets = mutableMapOf<Int, Bet>()
    private var cachedResult: BettingResult? = null

    /** Store a new bet, replacing any existing bet for the same match. */
    fun placeBet(bet: Bet) {
        bets[bet.matchId] = bet
        cachedResult = null
    }

    /**
     * Compare the stored bets with the supplied [matches]. A match is only counted
     * as "evaluated" when both a prediction and a final score are available.
     */
    fun evaluate(matches: List<Match>): BettingResult {
        cachedResult?.let { return it }
        var correct = 0
        var evaluated = 0
        for (match in matches) {
            val home = match.homeScore ?: continue
            val away = match.awayScore ?: continue
            val bet = bets[match.matchId] ?: continue
            evaluated++
            if (bet.prediction == Prediction.outcomeOf(home, away)) correct++
        }
        return BettingResult(correct, evaluated).also { cachedResult = it }
    }

    /**
     * Total bonus score across all stored bets:
     *   - 3 points if the stored bet has both predicted scores set and they match the actual scores exactly.
     *   - 1 point if the predicted outcome (home win, away win, draw) matches the actual outcome but the
     *     exact scores do not (or no score prediction was provided).
     *   - 0 points if the predicted outcome is wrong or the match has not been played.
     */
    fun evaluateBonus(matches: List<Match>): Int {
        // TODO("Implement bonus point evaluation")
        var totalBonus = 0

        for (match in matches) {
            val bet = bets[match.matchId]
            val homeScore = match.homeScore ?: continue
            val awayScore = match.awayScore ?: continue

            if (bet != null) {
                if (
                    bet.predictedHomeScore != null &&
                    bet.predictedAwayScore != null &&
                    bet.predictedHomeScore == homeScore &&
                    bet.predictedAwayScore == awayScore
                ) {
                    totalBonus += 3
                } else if (bet.prediction == Prediction.outcomeOf(homeScore, awayScore)) {
                    totalBonus += 1
                }
            }
        }
        println("You have got a bonus point of $totalBonus")
        return totalBonus
    }

    // return the bet in a specific group for the specific matchid so that the user can change or remove a bet
    fun showBet(matchId: Int): Bet? {
        val bet = bets[matchId]
        return bet
    }
    /**
     * Remove the bet for [matchId]. Does nothing if no bet exists for that match.
     */
    fun removeBet(matchId: Int) {
        // TODO("Implement removing a single bet by matchId")
        if (bets.containsKey(matchId)) {
            cachedResult = null
            bets.remove(matchId)
            println("\nThe bet for matchId $matchId is removed. Returning to the main menu...")
        } else {
            println("\nThe bet for matchId $matchId is not found.")
        }
    }

    /**
     * Change an existing bet. Only updates the bet if a bet for the same matchId already
     * exists; throws [IllegalArgumentException] if no bet is found for that match.
     */
    fun changeBet(bet: Bet) {
        // TODO("Implement changing an existing bet")
        println("You are about to change the bet for the selected match!")
        val code: Int = Console.readInt(
            "Your prediction (0 = Draw, 1 = Home win, 2 = Away win, 3 = Predict Home and Away Score): ",
            setOf(0, 1, 2, 3)
        )
        val newBet = if (code == 3) {
            val homeScore = Console.readInt("Predict Home Score: ")
            val awayScore = Console.readInt("Predict Away Score: ")

            Bet(
                matchId = bet.matchId,
                prediction = Prediction.outcomeOf(homeScore, awayScore),
                predictedHomeScore = homeScore,
                predictedAwayScore = awayScore
            )
        } else {
            Bet(
                matchId = bet.matchId,
                prediction = Prediction.fromCode(code)
            )
        }

        placeBet(newBet)

        println("\nThe bet for match Id ${bet.matchId} is changed. Returning to the main menu...")
    }

    /** Drop all stored bets. */
    fun clear() {
        bets.clear()
    }
}
