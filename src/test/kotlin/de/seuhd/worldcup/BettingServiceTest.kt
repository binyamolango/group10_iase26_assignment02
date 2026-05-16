package de.seuhd.worldcup

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

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

    fun captureOutput(block: () -> Unit): String {
        val output = ByteArrayOutputStream()
        val originalOut = System.out

        try {
            System.setOut(PrintStream(output))
            block()
            return output.toString().trim()
        } finally {
            System.setOut(originalOut)
        }
    }

    private fun withInput(input: String, block: () -> Unit) {
        val originalIn = System.`in`

        try {
            System.setIn(ByteArrayInputStream(input.toByteArray()))
            block()
        } finally {
            System.setIn(originalIn)
        }
    }

    // ── evaluateBonus ──────────────────────────────────────────────────────────

    @Test
    fun `evaluateBonus awards 3 points for an exact score prediction`() {
        // TODO("implement test")

        // arrange
        val matches = listOf(
            match(id = 1, home = "Germany", away = "Brazil", hs = 2, aws = 1),
            match(id = 2, home = "Portugal", away = "Argentina", hs = 1, aws = 3),
            match(id = 3, home = "Spain", away = "France", hs = 1, aws = 1)
        )

        val bet1 = Bet(1, Prediction.HOME_WIN, 2, 1)
        val bet2 = Bet(2, Prediction.AWAY_WIN, 1, 3)
        val bet3 = Bet(3, Prediction.DRAW, 1, 1)
        BettingService.placeBet(bet1)
        BettingService.placeBet(bet2)
        BettingService.placeBet(bet3)

        // act
        val result = BettingService.evaluateBonus(matches)

        // assert
        assertEquals(9, result)

        resetBets()
    }

    @Test
    fun `evaluateBonus awards 1 point for correct outcome without exact score`() {
        // TODO("implement test")

        // arrange
        val matches = listOf(
            match(id = 1, home = "Germany", away = "Brazil", hs = 2, aws = 1),
            match(id = 2, home = "Portugal", away = "Argentina", hs = 1, aws = 3),
            match(id = 3, home = "Spain", away = "France", hs = 1, aws = 1)
        )

        val bet1 = Bet(1, Prediction.HOME_WIN)
        val bet2 = Bet(2, Prediction.AWAY_WIN)
        val bet3 = Bet(3, Prediction.DRAW)
        BettingService.placeBet(bet1)
        BettingService.placeBet(bet2)
        BettingService.placeBet(bet3)

        // act
        val result = BettingService.evaluateBonus(matches)

        // assert
        assertEquals(3, result)

        resetBets()
    }

    @Test
    fun `evaluateBonus awards 0 points for a wrong prediction`() {
        // TODO("implement test")

        // arrange
        val matches = listOf(
            match(id = 1, home = "Germany", away = "Brazil", hs = 2, aws = 1),
            match(id = 2, home = "Portugal", away = "Argentina", hs = 1, aws = 3),
            match(id = 3, home = "Spain", away = "France", hs = 1, aws = 1)
        )

        val bet1 = Bet(1, Prediction.HOME_WIN) // right prediction (award 1 point)
        val bet2 = Bet(2, Prediction.HOME_WIN, 3, 1) // wrong prediction
        val bet3 = Bet(3, Prediction.AWAY_WIN) // wrong prediction (award 0 point)
        BettingService.placeBet(bet1)
        BettingService.placeBet(bet2)
        BettingService.placeBet(bet3)

        // act
        val result = BettingService.evaluateBonus(matches)

        // assert
        assertEquals(1, result)

        resetBets()
    }

    @Test
    fun `evaluateBonus ignores unplayed matches`() {
        // TODO("implement test")

        // arrange
        val matches = listOf(
            match(id = 1, home = "Germany", away = "Brazil", hs = null, aws = null),
            match(id = 2, home = "Portugal", away = "Argentina", hs = 1, aws = 3)
        )

        val bet1 = Bet(1, Prediction.HOME_WIN)
        val bet2 = Bet(2, Prediction.AWAY_WIN, 1, 3)
        BettingService.placeBet(bet1)
        BettingService.placeBet(bet2)

        // act
        val result = BettingService.evaluateBonus(matches)

        // assert
        assertEquals(3, result)

        resetBets()
    }

    // ── removeBet ─────────────────────────────────────────────────────────────

    @Test
    fun `removeBet removes an existing bet so it no longer affects evaluation`() {
        // TODO("implement test")

        // arrange
        val matchId = 1
        val matches = listOf(
            match(id = 1, home = "Germany", away = "Brazil", hs = 2, aws = 1),
            match(id = 2, home = "Portugal", away = "Argentina", hs = 1, aws = 3),
            match(id = 3, home = "Spain", away = "France", hs = 1, aws = 1)
        )
        val bet1 = Bet(1, Prediction.HOME_WIN, 2, 1)
        val bet2 = Bet(2, Prediction.AWAY_WIN, 1, 3)
        val bet3 = Bet(3, Prediction.DRAW, 1, 1)
        BettingService.placeBet(bet1)
        BettingService.placeBet(bet2)
        BettingService.placeBet(bet3)

        // act
        BettingService.removeBet(matchId)
        val bonusPoint = BettingService.evaluateBonus(matches)

        // assert
        assertEquals(6, bonusPoint) // bonusPoint should have been 9 if the bet hasn't been removed

        resetBets()
    }

    @Test
    fun `removeBet does nothing when no bet exists for that matchId`() {
        // TODO("implement test")

        // arrange
        val matchId = 1
        val removeMatchId = 37
        val bet = Bet(matchId = 1, prediction = Prediction.HOME_WIN)
        BettingService.placeBet(bet)
        val expectedErrorMsg = "The bet for matchId $removeMatchId is not found."
        val actualErrorMsg = captureOutput {
            BettingService.removeBet(removeMatchId)
        }

        // act
        BettingService.removeBet(removeMatchId)

        // assert
        assertEquals(bet, BettingService.showBet(matchId)) // to check the bet is the same (not removed)
        assertEquals(expectedErrorMsg, actualErrorMsg)
        resetBets()
    }

    // ── changeBet ─────────────────────────────────────────────────────────────

    @Test
    fun `changeBet updates the prediction for an existing bet`() {
        // TODO("implement test")

        // arrange
        val bet1 = Bet(1, Prediction.DRAW)
        val bet2 = Bet(2, Prediction.HOME_WIN, 2, 1)
        BettingService.placeBet(bet1)
        BettingService.placeBet(bet2)

        // act
        withInput("2\n") {
            BettingService.changeBet(bet1)
        }

        // assert
        assertEquals(Bet(1, Prediction.AWAY_WIN), BettingService.showBet(1))
        assertEquals(bet2, BettingService.showBet(2))

        resetBets()
    }

    @Test
    fun `changeBet throws when no bet exists for that matchId`() {
        // TODO("implement test")

        // arrange
        val bet1 = Bet(1, Prediction.DRAW)
        val bet2 = Bet(2, Prediction.HOME_WIN, 2, 1)
        BettingService.placeBet(bet1)
        BettingService.placeBet(bet2)
        val missingBet = Bet(99, Prediction.DRAW)

        // act
        val exception = assertFailsWith<IllegalArgumentException> {
            BettingService.changeBet(missingBet)
        }

        // assert
        assertEquals("No bet found for match Id 99!", exception.message)

        resetBets()
    }
}