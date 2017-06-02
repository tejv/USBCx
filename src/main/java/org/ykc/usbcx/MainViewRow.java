package org.ykc.usbcx;


public class MainViewRow {

	private Integer sno;
	private String ok;
	private String sop;
	private String msg;
	private Integer id;
	private String drole;
	private String prole;
	private Integer count;
	private Integer rev;
	private Integer duration;
	private Long delta;
	private Integer vbus;
	private String data;

	public MainViewRow(String ok, Integer duration, Long delta, Integer vbus) {
		this.ok = ok;
		this.duration = duration;
		this.delta = delta;
		this.vbus = vbus;
	}
	public Integer getSno() {
		return sno;
	}
	public void setSno(Integer sno) {
		this.sno = sno;
	}
	public String getOk() {
		return ok;
	}
	public void setOk(String ok) {
		this.ok = ok;
	}
	public String getSop() {
		return sop;
	}
	public void setSop(String sop) {
		this.sop = sop;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getDrole() {
		return drole;
	}
	public void setDrole(String drole) {
		this.drole = drole;
	}
	public String getProle() {
		return prole;
	}
	public void setProle(String prole) {
		this.prole = prole;
	}
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public Integer getRev() {
		return rev;
	}
	public void setRev(Integer rev) {
		this.rev = rev;
	}
	public Integer getDuration() {
		return duration;
	}
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	public Long getDelta() {
		return delta;
	}
	public void setDelta(Long delta) {
		this.delta = delta;
	}
	public Integer getVbus() {
		return vbus;
	}
	public void setVbus(Integer vbus) {
		this.vbus = vbus;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
}
