package br.com.avsm.mymemorygame

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {
    private val TAG = "ANDRE"
    private lateinit var birdImages: List<Bitmap>
    private val buttonIds = intArrayOf(
        R.id.card1, R.id.card2, R.id.card3, R.id.card4,
        R.id.card5, R.id.card6, R.id.card7, R.id.card8,
        R.id.card9, R.id.card10, R.id.card11, R.id.card12
    )
    private val imageAssociations = mutableMapOf<Int, Bitmap>() // Mapeia botões para imagens
    private var matchedPairs = mutableSetOf<Int>() // Rastreia os pares combinados
    private var firstClickedButton: ImageView? = null
    private var secondClickedButton: ImageView? = null
    private var matchedCount = 0

    private var tempoDecorrido: Long = 0
    private lateinit var handler: Handler
    private lateinit var updateTimer: Runnable
    private var difficulty = 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        difficulty = this.intent.getLongExtra("difficulty", 1000L)

        // Inicialize a lista de imagens de pássaros a partir da classe Bird
        val bird = Bird(this)
        birdImages = bird.getBirds()

        // Distribua as imagens nos cartões e associe-as aos botões
        distributeImages()

        // Configurar onClickListeners para os botões do grid
        for (buttonId in buttonIds) {
            val button = findViewById<ImageView>(buttonId)
            button.setOnClickListener { view ->
                onCardClicked(view as ImageView)
            }
        }

        handler = Handler()

        updateTimer = object : Runnable {
            override fun run() {
                tempoDecorrido += 1000 // Incrementa o tempo em 1 segundo (ou ajuste conforme necessário)
                atualizarTempoNoTextView()
                handler.postDelayed(this, 1000) // Executa este Runnable novamente após 1 segundo
            }
        }

        initTimer()
    }

    private fun distributeImages() {
        val pairs = mutableListOf<Bitmap>()
        val mutableBirdImages = birdImages.toMutableList()
        mutableBirdImages.shuffle()

        // Crie pares de figuras (cada figura deve aparecer duas vezes)
        for (i in 0 until 6) {
            pairs.add(mutableBirdImages[i])
            pairs.add(mutableBirdImages[i])
        }

        // Embaralhe novamente os pares para que estejam em ordem aleatória
        pairs.shuffle()

        // Distribua as imagens para os cartões e associe-as aos botões
        for (i in 0 until 12) {
            val button = findViewById<ImageView>(buttonIds[i])
            val cardIndex = i % 6
            val associatedImage = pairs[cardIndex]
            imageAssociations[button.id] = associatedImage
            button.background = BitmapDrawable(resources, associatedImage)
        }
    }

    private fun onCardClicked(button: ImageView) {
        Log.d(TAG, "onCardClicked")
        if (button.id !in imageAssociations.keys || button.id in matchedPairs) {
            // Botão inválido (já combinado ou clicado)
            return
        }

        val clickedImage = imageAssociations[button.id]

        if (firstClickedButton == null) {
            // Primeiro clique
            firstClickedButton = button
            firstClickedButton?.setImageBitmap(clickedImage)
        } else {
            // Segundo clique
            secondClickedButton = button
            secondClickedButton?.setImageBitmap(clickedImage)

            // Verificar se as imagens coincidem
            val firstButtonImage = imageAssociations[firstClickedButton!!.id]
            val secondButtonImage = imageAssociations[secondClickedButton!!.id]

            if (firstButtonImage == secondButtonImage) {
                // As imagens coincidem
                matchedPairs.add(firstClickedButton!!.id)
                matchedPairs.add(secondClickedButton!!.id)
                matchedCount += 2
                firstClickedButton = null
                secondClickedButton = null
                if (matchedCount == 12) {
                    matchedPairs = mutableSetOf()
                    matchedCount = 0
                    // Todas as cartas foram combinadas (jogo completo)
                    showFinishDialog()
                }
            } else {
                // As imagens não coincidem, volte a imagem original após um pequeno atraso
                val handler = Handler()
                handler.postDelayed({
                    firstClickedButton?.setImageResource(R.drawable.card)
                    secondClickedButton?.setImageResource(R.drawable.card)
                    firstClickedButton = null
                    secondClickedButton = null
                }, difficulty)
            }
        }
    }

    fun showFinishDialog() {
        handler.removeCallbacks(updateTimer)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Fim de jogo!\nTempo: ${tempoDecorridoEmTexto(tempoDecorrido)}")
        builder.setMessage("Parabéns! Deseja reiniciar?")
        //builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))

        builder.setPositiveButton("Reiniciar") { dialog, which ->
            rebootGame()
        }

        builder.setNegativeButton("Salvar Tempo") { dialog, which ->
            finishGame()
        }
        builder.show()
    }

    fun rebootGame() {
        val handler = Handler()
        handler.postDelayed({
            for (i in 0 until 12) {
                val button = findViewById<ImageView>(buttonIds[i])
                button.setImageResource(R.drawable.card)
            }
            distributeImages()
            tempoDecorrido = 0
            atualizarTempoNoTextView()
            initTimer()
        }, 1000)
    }

    fun finishGame() {
        this.startActivity(
            Intent(this, InitGame::class.java)
                .putExtra("tempoDecorrido", tempoDecorrido)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        this.finish()
    }

    private fun atualizarTempoNoTextView() {
        findViewById<TextView>(R.id.textTimer).text = tempoDecorridoEmTexto(tempoDecorrido)
    }

    private fun tempoDecorridoEmTexto(tempoDecorrido: Long): String {
        val segundos = (tempoDecorrido / 1000).toInt()
        val minutos = segundos / 60
        val segundosRestantes = segundos % 60
        return String.format("%02d:%02d", minutos, segundosRestantes)
    }

    private fun initTimer() {
        handler.post(updateTimer)
    }
}