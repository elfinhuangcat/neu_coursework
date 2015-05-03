import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public class TupleWritable implements WritableComparable<TupleWritable> { 
	private Text category = null;
	private DoubleWritable sale = null;
	
	public TupleWritable() {
		super();
	}
	
	public TupleWritable(String text, Double num) {
		super();
		this.category = new Text(text);
		this.sale = new DoubleWritable(num);
	}
	// Getter and Setter for category
	public Text getCategory() {
		return category;
	}
	public void setCategory(Text category) {
		this.category = category;
	}
	// Getter and Setter for sale
	public DoubleWritable getSale() {
		return sale;
	}
	public void setSale(DoubleWritable sale) {
		this.sale = sale;
	}
	@Override
	public void readFields(DataInput in) throws IOException {
		this.category = new Text(in.readUTF());
		this.sale = new DoubleWritable(in.readDouble());
		
	}
	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(this.category.toString());
		out.writeDouble(this.sale.get());
		
	}
	@Override
	public int compareTo(TupleWritable other) {
		// This piece of code comes from:
		// "http://tutorials.techmytalk.com/2014/11/14/
		//  mapreduce-composite-key-operation-part2/"
		if (other == null)
			return 0;
		int intcnt = this.category.compareTo(other.category);
		return intcnt == 0 ? this.sale.compareTo(other.sale) : intcnt;
	}
	@Override
	public String toString() {
		return "(" + this.category.toString() + ", " + this.sale.get() + ")";
	}
}