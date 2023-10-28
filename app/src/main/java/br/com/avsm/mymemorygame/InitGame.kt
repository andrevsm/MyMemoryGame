package br.com.avsm.mymemorygame

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InitGame : AppCompatActivity() {
    private val recordeAdapter = RecordeAdapter()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_init_game)

        sharedPreferences = getSharedPreferences("HistoricoTempo", Context.MODE_PRIVATE)

        findViewById<Button>(R.id.jogarButton).setOnClickListener {
            var difficulty = 1000L
            if (findViewById<RadioButton>(R.id.hardButton).isChecked) {
                difficulty = 300L
            }
            this.startActivity(
                Intent(this, GameActivity::class.java)
                    .putExtra("difficulty", difficulty)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        findViewById<Button>(R.id.resetButton).setOnClickListener {
            // Redefinir o histórico no SharedPreferences
            val editor = sharedPreferences.edit()
            editor.putString("historico", "")
            editor.apply()

            // Limpar o adaptador da RecyclerView
            recordeAdapter.limparRecorde()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recordeAdapter

        val tempoDecorrido = this.intent.getLongExtra("tempoDecorrido", 0L)
        if (tempoDecorrido != 0L) {
//            val recorde = Recorde(tempoDecorrido)
//            recordeAdapter.adicionarRecorde(recorde)
            val historico = sharedPreferences.getString("historico", "") ?: ""
            val novoHistorico = "$historico\n$tempoDecorrido"
            val editor = sharedPreferences.edit()
            editor.putString("historico", novoHistorico)
            editor.apply()
        }

        updateAdapter()
    }

    private fun updateAdapter() {
        val historico = sharedPreferences.getString("historico", "") ?: ""
        val tempos = historico.split("\n").mapNotNull { it.toLongOrNull() } ?: emptyList()
        val temposOrdenados = tempos.sorted()
        for (tempo in temposOrdenados) {
            recordeAdapter.adicionarRecorde(Recorde(tempo))
        }
    }

    data class Recorde(val tempo: Long)
    class RecordeAdapter : RecyclerView.Adapter<RecordeAdapter.ViewHolder>() {
        private val temposRecordes = mutableListOf<Recorde>()

        fun adicionarRecorde(recorde: Recorde) {
            temposRecordes.add(recorde)
            notifyDataSetChanged()
        }

        fun limparRecorde() {
            temposRecordes.clear()
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_recorde, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val recorde = temposRecordes[position]
            holder.textRecorde.text = "${position + 1} - ${formatarTempo(recorde.tempo)}"
        }

        override fun getItemCount(): Int {
            return temposRecordes.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textRecorde: TextView = itemView.findViewById(R.id.textRecorde)
        }

        private fun formatarTempo(tempo: Long): String {
            val segundos = tempo / 1000
            val minutos = segundos / 60
            val segundosRestantes = segundos % 60
            return String.format("%02d:%02d", minutos, segundosRestantes)
        }
    }
}