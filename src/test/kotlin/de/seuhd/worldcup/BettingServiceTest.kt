package de.seuhd.worldcup

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.Test

class BettingServiceTest {

    private fun match(id: Int, home: String, away: String, hs: Int?, aws: Int?) =
        Match(
            matchId = id,
            round = "Matchday 1",
            date = "2026-06-01",
            homeTeam = home,
            awayTeam = away,
            homeScore = hs,
            awayScore = aws,
            ground = "Test Stadium"
        )

    @BeforeTest
    fun resetBets() {
        BettingService.clear()
    }

    // ── evaluateBonus ──────────────────────────────────────────────────────────

    @Test
    fun `evaluateBonus awards 3 points for an exact score prediction`() {
        // TODO("implement test")

        // arrange
        val matches = listOf(
            match(
                id = 1,
                home = "Germany",
                away = "Brazil",
                hs = 2,
                aws = 1
            )
        )

        val bet = Bet(1, Prediction.HOME_WIN, 2, 1)
        BettingService.placeBet(bet)

        // act
        val result = BettingService.evaluateBonus(matches)

        // assert
        assertEquals(3, result)

    }

    @Test
    fun `evaluateBonus awards 1 point for correct outcome without exact score`() {
        // TODO("implement test")
    }

    @Test
    fun `evaluateBonus awards 0 points for a wrong prediction`() {
        // TODO("implement test")
    }

    @Test
    fun `evaluateBonus ignores unplayed matches`() {
        // TODO("implement test")
    }

    // ── removeBet ─────────────────────────────────────────────────────────────

    @Test
    fun `removeBet removes an existing bet so it no longer affects evaluation`() {
        // TODO("implement test")
    }

    @Test
    fun `removeBet does nothing when no bet exists for that matchId`() {
        // TODO("implement test")
    }

    // ── changeBet ─────────────────────────────────────────────────────────────

    @Test
    fun `changeBet updates the prediction for an existing bet`() {
        // TODO("implement test")
    }

    @Test
    fun `changeBet throws when no bet exists for that matchId`() {
        // TODO("implement test")
    }
}