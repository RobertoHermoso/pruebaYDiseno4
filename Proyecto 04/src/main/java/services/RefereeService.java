
package services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import repositories.RefereeRepository;
import security.Authority;
import security.LoginService;
import security.UserAccount;
import domain.Complaint;
import domain.Note;
import domain.Referee;
import domain.Report;

@Service
@Transactional
public class RefereeService {

	// Managed repository ------------------------------------------

	@Autowired
	private RefereeRepository		refereeRepository;

	// Supporting Services ------------------------------------------

	@Autowired
	private ComplaintService		complaintService;
	@Autowired
	private NoteService				noteService;
	@Autowired
	private ReportService			reportService;
	@Autowired
	private ActorService			actorService;
	@Autowired
	private ConfigurationService	configurationService;


	//Aux
	public Referee securityAndReferee() {
		UserAccount userAccount = LoginService.getPrincipal();
		String username = userAccount.getUsername();
		Referee loggedReferee = this.refereeRepository.getRefereeByUsername(username);

		List<Authority> authorities = (List<Authority>) loggedReferee.getUserAccount().getAuthorities();
		Assert.isTrue(authorities.get(0).toString().equals("REFEREE"));

		return loggedReferee;
	}

	// CRUD Methods -------------------------------------------------

	public Referee create(String name, String middleName, String surname, String photo, String email, String phoneNumber, String address, String userName, String password) {

		Referee referee = new Referee();
		referee = (Referee) this.actorService.createActor(name, middleName, surname, photo, email, phoneNumber, address, userName, password);
		List<Authority> authorities = new ArrayList<Authority>();
		referee.getUserAccount().setAuthorities(authorities);

		UserAccount userAccountReferee = new UserAccount();
		Authority authority = new Authority();
		authority.setAuthority(Authority.REFEREE);
		authorities.add(authority);

		return referee;
	}
	public Referee save(Referee referee) {
		UserAccount userAccount;
		userAccount = LoginService.getPrincipal();
		Assert.isTrue(userAccount.getAuthorities().contains("REFEREE"));

		// Comprobacion en todos los SAVE de los ACTORES
		Assert.isTrue(referee.getId() == 0 || userAccount.equals(referee.getUserAccount()));
		return this.refereeRepository.save(referee);
	}

	public Referee findOne(int refereeId) {
		return this.findOne(refereeId);
	}

	public void delete(Referee referee) {
		this.refereeRepository.delete(referee);
	}

	public List<Referee> findAll() {
		return this.refereeRepository.findAll();
	}

	public Referee getRefereeByUsername(String username) {
		return this.refereeRepository.getRefereeByUsername(username);
	}

	public Collection<Complaint> complaintsUnassigned() {
		return this.refereeRepository.complaintsUnassigned();
	}

	public Collection<Note> notesReferee(int refereeId) {
		return this.refereeRepository.notesReferee(refereeId);
	}

	// Methods ------------------------------------------------------

	//Testeado
	public List<Complaint> unassignedComplaints() {
		Referee loggedReferee = this.securityAndReferee();
		return (List<Complaint>) this.refereeRepository.complaintsUnassigned();
	}

	//Testeado
	public Complaint assingComplaint(Complaint complaint) {
		Referee loggedReferee = this.securityAndReferee();
		List<Complaint> unassignedComplaints = (List<Complaint>) this.refereeRepository.complaintsUnassigned();
		Complaint comp = new Complaint();
		for (Complaint c : unassignedComplaints)
			if (c == complaint)
				c = comp;
		Assert.notNull(comp);
		complaint.setReferee(loggedReferee);
		Complaint complaintSaved = this.complaintService.save(complaint);
		this.configurationService.isActorSuspicious(loggedReferee);
		return complaintSaved;
	}

	//Testeado
	public List<Complaint> selfAssignedComplaints() {
		Referee loggedReferee = this.securityAndReferee();
		List<Complaint> res = new ArrayList<>();
		List<Complaint> complaints = this.complaintService.findAll();
		for (Complaint c : complaints)
			if (c.getReferee() == loggedReferee) {
				Assert.notNull(c);
				res.add(c);
			}
		return res;
	}

	public Note writeNoteReport(Report report, String mandatoryComment, List<String> optionalComments) {
		Referee loggedReferee = this.securityAndReferee();
		List<Report> lr = loggedReferee.getReports();
		Report rep = new Report();
		for (Report r : lr)
			if (r == report)
				r = rep;
		Assert.notNull(rep);
		Note note = this.noteService.create(mandatoryComment, optionalComments);
		report.getNotes().add(note);
		this.reportService.save(report);
		this.configurationService.isActorSuspicious(loggedReferee);
		return note;
	}

	public Note writeComment(String comment, Note note) {
		Referee loggedReferee = this.securityAndReferee();
		List<Note> notes = (List<Note>) this.refereeRepository.notesReferee(loggedReferee.getId());
		Note no = new Note();
		for (Note n : notes)
			if (n == note)
				n = no;
		Assert.notNull(no);
		note.getOptionalComments().add(comment);
		this.noteService.save(note);
		this.configurationService.isActorSuspicious(loggedReferee);
		return note;
	}

	//Testing
	public Report writeReportRegardingComplaint(Complaint complaint, String description, List<String> attachments, List<Note> notes) {
		Referee loggedReferee = this.securityAndReferee();
		List<Complaint> lc = this.complaintService.findAll();
		Report rep = null;
		Complaint com = null;
		for (Complaint c : lc)
			if (c.getId() == complaint.getId() && c.getReferee().equals(loggedReferee) && complaint.getReferee().equals(loggedReferee)) {
				com = c;
				break;
			}
		Assert.notNull(com);

		rep = this.reportService.create(description, attachments, notes);
		Assert.notNull(rep);
		Report reportSaved = this.reportService.save(rep);

		List<Report> repList = loggedReferee.getReports();
		repList.add(rep);
		loggedReferee.setReports(repList);
		this.save(loggedReferee);

		List<Report> repList2 = com.getReports();
		repList2.add(rep);
		com.setReports(repList2);
		this.complaintService.save(com);

		//SIN USAR POR AHORA, PENDIENTE DE REVISAR
		//this.configurationService.isActorSuspicious(loggedReferee);
		return reportSaved;
	}

	public Report modifiedReport(Report report) {
		Referee loggedReferee = this.securityAndReferee();
		Assert.isTrue(!report.getFinalMode());
		Report rp = this.reportService.save(report);
		this.configurationService.isActorSuspicious(loggedReferee);
		return rp;
	}

	public void eliminateReport(Report report) {
		Referee loggedReferee = this.securityAndReferee();
		Assert.isTrue(!report.getFinalMode());
		this.reportService.delete(report);
		this.configurationService.isActorSuspicious(loggedReferee);
	}

}
