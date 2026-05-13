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
        /**
         * return the total bonus score across all stored bets
         * for each bet the bonus score is determined (3 points, 1 point, and 0 point)
         * step 1: loop through the stored bets (handle null)
         * step 2: give 3 points for that specific bet
                - if the stored bet has both predicted scores set (for home and away)
            (check the place bet method)
                - and match exactly with the actual outcome (check for match data set)
         * step 3: give 1 point
                - if the predicted outcome matches the actual outcome
            (check the place bet method, placed bets, and matches data set (for the actual outcome))
         * step 4: give 0 point
                - if the predicted outcome doesn't match the actual outcome
                - or the match has not been played
         * step 5: return the total bonus score across all stored bets
        */
        println(bets)

        return 6
    }

    /**
     * Remove the bet for [matchId]. Does nothing if no bet exists for that match.
     */
    fun removeBet(matchId: Int) {
        TODO("Implement removing a single bet by matchId")
    }

    /**
     * Change an existing bet. Only updates the bet if a bet for the same matchId already
     * exists; throws [IllegalArgumentException] if no bet is found for that match.
     */
    fun changeBet(bet: Bet) {
        TODO("Implement changing an existing bet")
    }

    /** Drop all stored bets. */
    fun clear() {
        bets.clear()
    }
}
