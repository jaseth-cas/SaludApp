package pa.ac.utp.miprimeraapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MedicationAdapter(
    private val medications: MutableList<Medication>,
    private val onDeleteClick: (Medication, Int) -> Unit
) : RecyclerView.Adapter<MedicationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemMedName)
        val tvDose: TextView = view.findViewById(R.id.tvItemMedDose)
        val tvTime: TextView = view.findViewById(R.id.tvItemMedTime)
        val tvDate: TextView = view.findViewById(R.id.tvItemMedDate)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteMed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicamento, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val med = medications[position]
        holder.tvName.text = med.nombre
        holder.tvDose.text = med.dosis
        holder.tvTime.text = med.hora
        holder.tvDate.text = "Hasta ${med.fechaFin}"

        holder.btnDelete.setOnClickListener {
            onDeleteClick(med, position)
        }
    }

    override fun getItemCount() = medications.size
}

data class Medication(
    val id: Int,
    val nombre: String,
    val dosis: String,
    val hora: String,
    val fechaFin: String
)