const MAIN_PAGE = "index.html";
const LOGIN_PAGE = "login.html";

const SERVER_URL = "http://192.168.0.20:8080/gr3_dupas_gaspar/";

//Alert const
const ALERT_TITLE = ".alert-title";
const ALERT_CONTENT = ".alert-content";

const CONNEXION_ALERT = "#alert-connexion";

const CONNEXION_ERROR_TITLE = "Echec de la connexion";
const CONNEXION_ERROR_BAD_LOGIN_CONTENT = "Mauvais login et/ou mot de passe.";

const USER_LOGIN_ID = "#User-login";
const USER_NAME_ID = "#User-name";
const USER_FOLLOWS_ID = "#Follows-v";
const USER_FOLLOWERS_ID = "#Followers-v";
const USER_COMMENTS_ID = "#Comments-v";

const COMMENTS_MAIN_CONTAINER = "#Messages";

const AVATAR_MAIN = ".avatar-main";

const CRT_USER_CK = "crtUser";


//Server args
const LOGIN_URL = "user/login";
const LOGIN_LOGIN = "login";
const LOGIN_PASSWORD = "password";

const DEFAULT_AVATAR_SIZE = 150;

//Debug user
const DEBUG_ID = 1;
const DEBUG_LOGIN = "debug";
const DEBUG_PASSWORD = "password";
const DEBUG_FNAME = "Alan";
const DEBUG_LNAME = "Turing";
const DEBUG_CONTACT = false;

const DEBUG_JSON_CONNECTION = '{"nbMessages":8,"lName":"Turing","fName":"Allan","nbFollows":1,"nbFollowers":0,"avatar":"https:\/\/en.gravatar.com\/avatar\/8f1373a6019b6427d12726137fc2935e.jpg?d=mm","login":"debug","userId":5,"key":"91a5c0dc422d4f4b9f01a168e766d91f","email":"alexandregaspardcilia@hotmail.fr"}\n';


//Debug comment
//function Comment(id, author, content, date, score) {
const DEBUG_M_ID = 1;
const DEBUG_M_AUTHOR = 5;
const DEBUG_M_CONTENT = "Message de debug. https://open.spotify.com/artist/63MQldklfxkjYDoUE4Tppz Lorem ipsum dolor sit consectetur elit. Donec a diam lectus.";
const DEBUG_M_DATE = "00-00-00 00:00:00";
const DEBUG_M_SCORE = 0;


function init() {
	
	environnement = new Object();
	environnement.users = {};
	
	var debugUser = User.parseJSON(DEBUG_JSON_CONNECTION);
	
	environnement.users[debugUser.id+""] = debugUser;
	
	setupIntegrationEnv();
	
	
	if(Cookies.get(CRT_USER_CK) != undefined) {
		setConnectedUserUI();
		fillIndexComments();
	} else {
		window.location.href = LOGIN_PAGE;
	}
	
}

function setupIntegrationEnv() {
	environnement.integration = {};
	
	for (var i = 0; i < regex.length; i++) {
		environnement.integration[i] = new Intregration(regex[i], replacer[i]);
	}
	
}

function setConnectedUserUI() {
	var user = Cookies.getJSON(CRT_USER_CK);
	
		
	var login = user["login"];
	var name = user["fName"] + " " + user["lName"];
	
	
	$(USER_LOGIN_ID).first().text("@" + login);
	$(USER_NAME_ID).first().text(name);
	
	
	(user["nbFollowers"] != undefined) ? $(USER_FOLLOWERS_ID).first().text(user["nbFollowers"]+"") : $(USER_FOLLOWERS_ID).first().text("0");
	(user["nbFollows"] != undefined) ? $(USER_FOLLOWS_ID).first().text(user["nbFollows"]+"") : $(USER_FOLLOWS_ID).first().text("0");	
	(user["nbComments"] != undefined) ? $(USER_COMMENTS_ID).first().text(user["nbComments"]+"") : $(USER_COMMENTS_ID).first().text("0");
	
	$(AVATAR_MAIN).each(function() {
		$(this).attr("src", addAvatarSize(user["avatar"], DEFAULT_AVATAR_SIZE));
	});

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
	
	for (var i = 0; i < 5; i++) {
		result[i] = new Comment(i, DEBUG_M_AUTHOR, DEBUG_M_CONTENT, DEBUG_M_DATE, DEBUG_M_SCORE);
	}
	
	
	return result;
}


function connexion(form) {
	var login = form.login.value;
	var password = form.password.value;
	
	
	
	if (connect(login, password)) {
		
		
		
			

		
		
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
	var jsonString = DEBUG_JSON_CONNECTION;
	var user;
/*
	var request = $.ajax({
		url: SERVER_URL + LOGIN_URL,
		type: "post",
		data: {LOGIN_LOGIN : login, LOGIN_PASSWORD : password},
		dataType: "jsonp",
		
		success: function(data) {
			console.log(data);
		}, 
		
		error: function (xhr, ajaxOptions, thrownError) {
		    console.log(xhr);
		    console.log(ajaxOptions);
		    console.log(thrownError);
      	}
	
	});
*/	

	
	user = User.parseJSON(jsonString);
	
	
	Cookies.set(CRT_USER_CK, user.toArray());
	
	
	environnement.users[user.id+""] = user; 
	window.location.href = MAIN_PAGE; 		
	
	return true;
}


function defineAlert(alertId, alertTitle, alertContent) {
	$(alertId + " " + ALERT_TITLE).first().text(alertTitle);
	$(alertId + " " + ALERT_CONTENT).first().text(alertContent);
}


function hideAlert(element) {
	$(element).css("display", "none");
}


function User(id, login, fName, lName, avatar, contact) {
	this.id = id;
	this.login = login;
	this.fName = fName;
	this.lName = lName;
	this.avatar = avatar;
	
	this.contact = false;
	if (contact != undefined)
		this.contact = contact;
	
	environnement.users[id] = this;
	
	return this;
}

User.parseJSON = function(jsonString) {
	j = $.parseJSON(jsonString);
	
	var result = new User(j.userId, j.login, j.fName, j.lName, j.avatar, false);
	
	if(j.nbFollows != undefined)
		result.nbFollows = j.nbFollows;
	
	if(j.nbFollowers != undefined)
		result.nbFollowers = j.nbFollowers;
	
	if(j.nbMessages != undefined)
		result.nbComments = j.nbMessages;

	

	
	return result;
}

function addAvatarSize(avatar, size) {
	return avatar + "&size=" + size;
}

User.prototype.modifStatus = function() {
	this.contact = !this.contact;
}

User.prototype.toArray = function() {
	var result = {"id":this.id, "login":this.login, "fName":this.fName,
					 "lName":this.lName, "avatar": this.avatar, "contact":this.contact};
					 
	if(this.nbFollows != undefined)
		result["nbFollows"] = this.nbFollows;
	
	if(this.nbFollowers != undefined)
		result["nbFollowers"] = this.nbFollowers;
	
	if(this.nbComments != undefined)
		result["nbComments"] = this.nbComments;
		
	
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
	var integratedContent;
	var media = "";
	var auth = environnement.users[this.author+""];
	
	if(auth == undefined) {
		//TODO Get author here
	}
	
	
	integratedContent = this.content;
	
	var stopSearch = false;
	var crt;
	var matchValues;
	
	
	for (var i in environnement.integration) {
		crt = environnement.integration[i];

		matchValues = this.content.match(crt.regex);
		
		for(var j in matchValues) {
			crtMatch = matchValues[j];
			
			crtMatch = crtMatch.replace(/\?/gim, "\\?");
		
			integratedContent = integratedContent.replace(new RegExp(crtMatch, "igm"), crt.getHtml(matchValues[j]));
		}
			
	}
	
	if (this.content.match(Youtube.regex) != null) {
		media = Youtube.toHtml(this.content.match(Youtube.regex)[0]);
	} else if (this.content.match(Spotify.regex) != null) {
		media = Spotify.toHtml(this.content.match(Spotify.regex)[0]);
	} else if (this.content.match(Image.regex) != null) {
		media = Image.toHtml(this.content.match(Image.regex)[0]);
	} else if (this.content.match(Video.regex) != null) {
		media = Video.toHtml(this.content.match(Video.regex)[0]);
	} else if (this.content.match(Audio.regex) != null) {
		media = Audio.toHtml(this.content.match(Audio.regex)[0]);
	}

	
	result += '<div class="Message">\n';
	result += '<img src="' + auth.avatar + '" class="MsgAvatar thumbnail" />\n';
	result += '<div class="MsgMain">\n';
	result += '<p>\n';
	result += '<span class="MsgAuthor">' + auth.fName + " " + auth.lName + '</span>\n';
	result += '<span class="MsgLogin">@' + auth.login + '</span>\n';
	result += '</p>\n';
	result += '<p class="MsgContent" >\n';
	result += integratedContent;
	result += '</p>\n';
	result += '<div class="media-container">' + media + "</div>";
	result += '</div>';
	result += '</div>';
	
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









	




