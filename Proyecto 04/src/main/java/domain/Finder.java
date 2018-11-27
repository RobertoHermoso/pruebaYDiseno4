
package domain;

import java.util.Date;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.Past;

@Entity
@Access(AccessType.PROPERTY)
public class Finder extends DomainEntity {

	private String			keyWord;
	private String			category;
	private String			warranty;
	private double			minPrice;
	private double			maxPrice;
	private Date			startDate;
	private Date			endDate;

	private List<FixUpTask>	fixUpTasks;


	@Valid
	public String getKeyWord() {
		return this.keyWord;
	}

	public void setKeyWord(String keyWord) {
		this.keyWord = keyWord;
	}
	@ManyToMany
	@Valid
	public List<FixUpTask> getFixUpTasks() {
		return this.fixUpTasks;
	}

	public void setFixUpTasks(List<FixUpTask> fixUpTasks) {
		this.fixUpTasks = fixUpTasks;
	}

	@Valid
	public String getCategory() {
		return this.category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Valid
	public String getWarranty() {
		return this.warranty;
	}

	public void setWarranty(String warranty) {
		this.warranty = warranty;
	}

	@Valid
	@Min(0)
	@Digits(fraction = 2, integer = 9)
	public double getMinPrice() {
		return this.minPrice;
	}

	public void setMinPrice(double minPrice) {
		this.minPrice = minPrice;
	}

	@Valid
	@Digits(fraction = 2, integer = 9)
	public double getMaxPrice() {
		return this.maxPrice;
	}

	public void setMaxPrice(double maxPrice) {
		if (maxPrice >= this.minPrice)
			this.maxPrice = maxPrice;
	}

	@Past
	@Valid
	@Temporal(TemporalType.TIMESTAMP)
	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	@Temporal(TemporalType.TIMESTAMP)
	@Valid
	public Date getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

}
