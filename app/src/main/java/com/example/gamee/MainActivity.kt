package com.example.gamee

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var scoreText: TextView
    private lateinit var paddle: View
    private lateinit var ball: View
    private lateinit var brickContainer: LinearLayout
    private lateinit var newGameButton: Button

    private var ballX = 0f
    private var ballY = 0f
    private var ballSpeedX = 0f
    private var ballSpeedY = 0f

    private var paddleX = 0f

    private var score = 0
    private var lives = 1

    private val brickRows = 4
    private val brickColumns = 10
    private val brickWidth = 100
    private val brickHeight = 40
    private val brickMargin = 4

    private var isGameOver = false

    private fun resetGame() {
        score = 0
        lives = 1
        isGameOver = false
        scoreText.text = "Score: $score"
        brickContainer.removeAllViews()
        initializeBricks()
        resetBallPosition()
        start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scoreText = findViewById(R.id.scoreText)
        paddle = findViewById(R.id.paddle)
        ball = findViewById(R.id.ball)
        brickContainer = findViewById(R.id.brickContainer)

        val newgame = findViewById<Button>(R.id.start)

        newgame.setOnClickListener {
            setContentView(R.layout.activity_game)
            initializeGameViews()
            resetGame()
            newgame.visibility = View.INVISIBLE
        }

    }
    private fun initializeGameViews() {
        scoreText = findViewById(R.id.scoreText)
        paddle = findViewById(R.id.paddle)
        ball = findViewById(R.id.ball)
        brickContainer = findViewById(R.id.brickContainer)
    }
    private fun initializeBricks() {
        val brickWidthWithMargin = brickWidth + brickMargin

        for (row in 0 until brickRows) {
            val rowLayout = LinearLayout(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowLayout.layoutParams = params

            for (col in 0 until brickColumns) {
                val brick = View(this)
                val brickParams = LinearLayout.LayoutParams(brickWidth, brickHeight)
                brickParams.setMargins(brickMargin, brickMargin, brickMargin, brickMargin)
                brick.layoutParams = brickParams
                brick.setBackgroundResource(R.drawable.ic_launcher_background)
                rowLayout.addView(brick)
            }

            brickContainer.addView(rowLayout)
        }
    }

    private fun moveBall() {
        ballX += ballSpeedX
        ballY += ballSpeedY

        ball.x = ballX
        ball.y = ballY
    }

    private fun movePaddle(x: Float) {
        paddleX = x - paddle.width / 2
        paddle.x = paddleX
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun checkCollision() {
        if (isGameOver) return

        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        // Check collision with walls
        if (ballX <= 0 || ballX + ball.width >= screenWidth) {
            ballSpeedX *= -1
        }

        if (ballY <= 0) {
            ballSpeedY *= -1
        }

        // Check collision with paddle
        if (ballY + ball.height >= paddle.y && ballY + ball.height <= paddle.y + paddle.height
            && ballX + ball.width >= paddle.x && ballX <= paddle.x + paddle.width
        ) {
            ballSpeedY *= -1
            score++
            scoreText.text = "Score: $score"
        }

        // Check collision with bricks
        for (row in 0 until brickRows) {
            val rowLayout = brickContainer.getChildAt(row) as LinearLayout
            for (col in 0 until brickColumns) {
                val brick = rowLayout.getChildAt(col) as View
                if (brick.visibility == View.VISIBLE) {
                    val brickLeft = brick.x + rowLayout.x
                    val brickRight = brickLeft + brick.width
                    val brickTop = brick.y + rowLayout.y
                    val brickBottom = brickTop + brick.height

                    if (ballX + ball.width >= brickLeft && ballX <= brickRight
                        && ballY + ball.height >= brickTop && ballY <= brickBottom
                    ) {
                        brick.visibility = View.INVISIBLE
                        ballSpeedY *= -1
                        score++
                        scoreText.text = "Score: $score"
                    }
                }
            }
        }

        // Check collision with bottom wall (paddle misses the ball)
        if (ballY + ball.height >= screenHeight) {
            lives--

            if (lives > 0) {
                Toast.makeText(this, "$lives balls left", Toast.LENGTH_SHORT).show()
            }

            if (lives <= 0 || isAllBricksCleared()) {
                gameOver()
                return
            } else {
                resetBallPosition()
                start()
            }
        }
    }

    private fun isAllBricksCleared(): Boolean {
        for (row in 0 until brickRows) {
            val rowLayout = brickContainer.getChildAt(row) as LinearLayout
            for (col in 0 until brickColumns) {
                val brick = rowLayout.getChildAt(col) as View
                if (brick.visibility == View.VISIBLE) {
                    return false
                }
            }
        }
        return true
    }

    private fun resetBallPosition() {
        val displayMetrics = resources.displayMetrics

        ballX = displayMetrics.widthPixels.toFloat() / 2 - ball.width / 2
        ballY = displayMetrics.heightPixels.toFloat() / 2 - ball.height / 2

        ball.x = ballX
        ball.y = ballY

        // Set initial speed along both X and Y axes (diagonally towards bottom-right)
        ballSpeedX = 5f
        ballSpeedY = 5f
    }


    private fun gameOver() {
        isGameOver = true
        Toast.makeText(this, "Game Over. Score: $score", Toast.LENGTH_SHORT).show()
        val newgame = findViewById<Button>(R.id.newgame)
        newgame.visibility = View.VISIBLE

        newgame.setOnClickListener {
            setContentView(R.layout.activity_game)
            initializeGameViews()
            resetGame()
            newgame.visibility = View.INVISIBLE
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun movePaddle() {
        paddle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    movePaddle(event.rawX)
                }
            }
            true
        }
    }

    private fun start() {
        movePaddle()

        val displayMetrics = resources.displayMetrics

        paddleX = displayMetrics.widthPixels.toFloat() / 2 - paddle.width / 2
        paddle.x = paddleX

        ballX = displayMetrics.widthPixels.toFloat() / 2 - ball.width / 2
        ballY = displayMetrics.heightPixels.toFloat() / 2 - ball.height / 2

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = Long.MAX_VALUE
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            moveBall()
            checkCollision()
        }
        animator.start()
    }
}

