const MAIN_PAGE = "index.html";
const LOGIN_PAGE = "login.html";

//Alert const
const ALERT_TITLE = ".alert-title";
const ALERT_CONTENT = ".alert-content";

const CONNEXION_ALERT = "#alert-connexion";

const CONNEXION_ERROR_TITLE = "Echec de la connexion";
const CONNEXION_ERROR_BAD_LOGIN_CONTENT = "Mauvais login et/ou mot de passe.";

const USER_LOGIN_ID = "#User-login";
const USER_NAME_ID = "#User-name";

const COMMENTS_MAIN_CONTAINER = "#Messages";


const CRT_USER_CK = "crtUser";

//Debug user
const DEBUG_ID = 1;
const DEBUG_LOGIN = "debug";
const DEBUG_FNAME = "Alan";
const DEBUG_LNAME = "Turing";
const DEBUG_CONTACT = false;

//Debug comment
//function Comment(id, author, content, date, score) {
const DEBUG_M_ID = 1;
const DEBUG_M_AUTHOR = 1;
const DEBUG_M_CONTENT = "Message de debug. Lorem ipsum dolor sit consectetur elit. Donec a diam lectus.";
const DEBUG_M_DATE = "00-00-00 00:00:00";
const DEBUG_M_SCORE = 0;


function init() {
	
	environnement = new Object();
	environnement.users = {};
	
	var debugUser = new User(DEBUG_ID, DEBUG_LOGIN, DEBUG_FNAME, DEBUG_LNAME, DEBUG_CONTACT);
	
	environnement.users[debugUser.id+""] = debugUser;
	
	
	if(Cookies.get(CRT_USER_CK) != undefined) {
		setConnectedUserUI();
		fillIndexComments();
	}
	
}

function setConnectedUserUI() {
	var user = Cookies.getJSON(CRT_USER_CK);	
		
	var login = user["login"];
	var name = user["fName"] + " " + user["lName"];
	
	
	$(USER_LOGIN_ID).first().text("@" + login);
	$(USER_NAME_ID).first().text(name);
}

function fillIndexComments() {
	var comments = getDebugComments();
	
	
	for (var i in comments) {
		$(COMMENTS_MAIN_CONTAINER).prepend(comments[i].getHtml());
	}
}

function gotoIndex() {
	window.location.href = MAIN_PAGE;
}

function getDebugComments() {
	var result = {};
	
	for (var i = 0; i < 10; i++) {
		result[i] = new Comment(i, DEBUG_M_AUTHOR, DEBUG_M_CONTENT, DEBUG_M_DATE, DEBUG_M_SCORE);
	}
	
	
	return result;
}


function connexion(form) {
	var login = form.login.value;
	var password = form.password.value;
	
	
	
	if (connect(login, password)) {
		var user = new User(DEBUG_ID, DEBUG_LOGIN, DEBUG_FNAME, DEBUG_LNAME, DEBUG_CONTACT);
		Cookies.set(CRT_USER_CK, user.toArray());
		
		environnement.users[user.id+""] = user; 
		
			
		window.location.href = MAIN_PAGE; 		
		
		
	} else {
		defineAlert(CONNEXION_ALERT, CONNEXION_ERROR_TITLE,
				 CONNEXION_ERROR_BAD_LOGIN_CONTENT);
	
		$(CONNEXION_ALERT).show();
	}
			
	
	
	
}

function disconnect() {
	//TODO send disconnection request to the server
	
	Cookies.remove(CRT_USER_CK);
	
	window.location.href = LOGIN_PAGE;
}

function connect(login, password) {
	//TODO	
	return true;
}


function defineAlert(alertId, alertTitle, alertContent) {
	$(alertId + " " + ALERT_TITLE).first().text(alertTitle);
	$(alertId + " " + ALERT_CONTENT).first().text(alertContent);
}


function hideAlert(element) {
	$(element).css("display", "none");
}


function User(id, login, fName, lName, contact) {
	this.id = id;
	this.login = login;
	this.fName = fName;
	this.lName = lName;
	
	this.contact = false;
	if (contact != undefined)
		this.contact = contact;
	
	environnement.users[id] = this;
	
	return this;
}

User.prototype.modifStatus = function() {
	this.contact = !this.contact;
}

User.prototype.toArray = function() {
	var result = {"id":this.id, "login":this.login, "fName":this.fName,
					 "lName":this.lName, "contact":this.contact};
	
	return result;
}


function Comment(id, author, content, date, score) {
	this.id = id;
	this.author = author;
	this.content = content;
	this.date = date;
	this.score = 0;
	
	if(score != undefined)
		this.score = score;
	
	
	
	
}

Comment.prototype.getHtml = function() {
	var result = "";

	var auth = environnement.users[this.author+""];
	
	if(auth == undefined) {
		//TODO Get author here
	}
	
	
	/*
	<div class="Message">
		<img src="img/100x100PH.png" class="MsgAvatar thumbnail" />
		<div class="MsgMain">
			<p>
				<span class="MsgAuthor" >NOM Prenom</span>
				<span class="MsgLogin" >@login</span>
			</p>
			<p class="MsgContent" >
				Lorem ipsum dolor sit consectetur elit. Donec a diam lectus. Sed sit amet ipsum mauris. Maecenas congue ligula ac quam viverra nec consectetur ante hendrerit. Donec et mollis dolor. 
			</p>
		</div>
	</div>
	*/
	
	result += '<div class="Message">\n';
	result += '<img src="img/100x100PH.png" class="MsgAvatar thumbnail" />\n';
	result += '<div class="MsgMain">\n';
	result += '<p>\n';
	result += '<span class="MsgAuthor">' + auth.fName + " " + auth.lName + '</span>\n';
	result += '<span class="MsgLogin">' + auth.login + '</span>\n';
	result += '</p>\n';
	result += '<p class="MsgContent" >\n';
	result += this.content;
	result += '</p>\n';
	result += '</div>';
	result += '</div>';
	
	/*
	s+= "<p>Put html here.</p>";
	//this.date.format('dd/mm/yyyy HH:MM:ss'); pour formater la date.
	
	//Faire attention à ce qu'il y est un utilisateur connecté pour afficher ce genre de chose.
	if (this.author.contact) {
		s+= "<p>Put contact related content here.</p>";
	}
	
	*/
	
	return result;
}


function SearchResults(results, query, contactOnly, author, date) {
	this.result = result;
	this.contactOnly = contactOnly;
	this.author = author;
	
	this.date = new Date();
	if (date != undefined)
		this.date = date;
	
	this.query = query;
	
	environnement.searchResults = this;	
}

SearchResults.prototype.getHtml = function(){
	var result = "";
	
	result += "<p>Put html here.</p>";
	
	for (var i in this.results) {
		results += "<p>Put more html here.</p>";
	}
	
	return result;
	
}


SearchResults.fromJSON = function(json) {
	var result = JSON.parse(s, SearchResults.revival);
	
	if (result.error != undefined) {
		alert(result.error);
		
		return undefined;
	}
	
	return result;
	
}


SearchResults.revival = function(key, value) {
	switch (key) {
		case "date" :
			return new Date(value);
		case "userid" :	
			return new User(value.id, value.login, value.contact);
		case "" :
			return new SearchResults(value.results, value.query, value.contactOnly, value.author, value.date);
			
		default :
			if (isNumber(key)) {
				return new Comment(value.id, value.author, value.content, value.date, value.score);
			} else {
				return value;	
			}
	}
}












	



