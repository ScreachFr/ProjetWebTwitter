const MAIN_PAGE = "index.html";
const LOGIN_PAGE = "login.html";

const SERVER_URL = " http://li328.lip6.fr:8280/gr3_dupas_gaspar/";

//Alert const
const ALERT_TITLE = ".alert-title";
const ALERT_CONTENT = ".alert-content";

const CONNEXION_ALERT = "#alert-connexion";

const CONNEXION_ERROR_TITLE = "Echec de la connexion";
const CONNEXION_ERROR_BAD_LOGIN_CONTENT = "Mauvais login et/ou mot de passe.";
const SERVER_ERROR_CONTENT = "Erreur server.";

const USER_LOGIN_ID = "#User-login";
const USER_NAME_ID = "#User-name";
const USER_FOLLOWS_ID = "#Follows-v";
const USER_FOLLOWERS_ID = "#Followers-v";
const USER_COMMENTS_ID = "#Comments-v";

const COMMENTS_MAIN_CONTAINER = "#Messages";

const AVATAR_MAIN = ".avatar-main";

const CRT_USER_CK = "crtUser";


//Server args
const CONNECTION_TEST_URL = "connection/test";

const LOGIN_URL = "user/login";
const LOGIN_LOGIN = "login";
const LOGIN_PASSWORD = "password";

const DEFAULT_AVATAR_SIZE = 150;

const DISCONNET_URL = "user/logout";

const GETUSER_URL = "user/get";
const GETUSER_ID = "id";
const GETUSER_YOUR_ID = "yourid";

const REGISTER_URL = "user/create";

const REGISTER_LOGIN = "login";
const REGISTER_PASSWORD = "password";
const REGISTER_FNAME = "fname";
const REGISTER_LNAME = "lname";
const REGISTER_MAIL = "email";

//Debug user
const DEBUG_ID = 5;
const DEBUG_LOGIN = "debug";
const DEBUG_PASSWORD = "password";
const DEBUG_FNAME = "Alan";
const DEBUG_LNAME = "Turing";
const DEBUG_CONTACT = false;

const DEBUG_JSON_CONNECTION = '{"nbMessages":8,"lName":"Turing","fName":"Allan","nbFollows":1,"nbFollowers":0,"avatar":"https:\/\/en.gravatar.com\/avatar\/8f1373a6019b6427d12726137fc2935e.jpg?d=mm","login":"debug","userId":5,"key":"91a5c0dc422d4f4b9f01a168e766d91f","email":"alexandregaspardcilia@hotmail.fr"}\n';
const DEBUG_JSON_CONNECTION_KO = '{"errorMessage":"Bad login or password","errorCode":1}';

//Debug comment
//function Comment(id, author, content, date, score) {
const DEBUG_M_ID = 1;
const DEBUG_M_AUTHOR = 6;
const DEBUG_M_CONTENT = "Message de debug. https://open.spotify.com/artist/63MQldklfxkjYDoUE4Tppz Lorem ipsum dolor sit consectetur elit. Donec a diam lectus.";
const DEBUG_M_DATE = "00-00-00 00:00:00";
const DEBUG_M_SCORE = 0;


function init() {
	
	environnement = new Object();
	environnement.users = {};
	
	var debugUser = User.fromJSON($.parseJSON(DEBUG_JSON_CONNECTION));
	
	environnement.users[debugUser.id+""] = debugUser;
	environnement.errorUser = new User(-1, "Error", "Unknown", "User" 
		, "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mm"
		, false);
	
	if(Cookies.get(CRT_USER_CK) !== undefined) {
		environnement.crtUser = User.fromJSON(Cookies.getJSON(CRT_USER_CK));
		
		setConnectedUserUI();
		fillIndexComments();
	} else {
		var sp = window.location.href.split("/");
	
		if(sp[sp.length-1] != LOGIN_PAGE) {
			window.location.href = LOGIN_PAGE;
		}
	}
	
}

function setConnectedUserUI() {
	var user = environnement.crtUser;
	
	var login = user.login;
	var name = user.fName + " " + user.lName;
	
	
	
	$(USER_LOGIN_ID).first().text("@" + login);
	$(USER_NAME_ID).first().text(name);
	
	
	(user.nbFollowers != undefined) ? $(USER_FOLLOWERS_ID).first().text(user.nbFollowers+"") : $(USER_FOLLOWERS_ID).first().text("0");
	(user.nbFollows != undefined) ? $(USER_FOLLOWS_ID).first().text(user.nbFollows+"") : $(USER_FOLLOWS_ID).first().text("0");	
	(user.nbComments != undefined) ? $(USER_COMMENTS_ID).first().text(user.nbComments+"") : $(USER_COMMENTS_ID).first().text("0");
	
	$(AVATAR_MAIN).each(function() {
		$(this).attr("src", addAvatarSize(user.avatar, DEFAULT_AVATAR_SIZE));
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
	
	
	var result = connect(login, password);
	
}

function disconnect() {
	
	Cookies.remove(CRT_USER_CK);
	
	
	var request = $.ajax({
		url: SERVER_URL + DISCONNET_URL,
		type: 'post',
		data: LOGIN_LOGIN + "=" + login,
		dataType: "json",
		async: false,
		success: function(data) {
		}, 
		
		error: function (xhr, ajaxOptions, thrownError) {
      	}
	
	});
	

	
	window.location.href = LOGIN_PAGE;	
	
	return false;
}

function connect(login, password) {
	var user;
	var j;
	var request = $.ajax({
		url: SERVER_URL + LOGIN_URL,
		type: 'post',
		data: LOGIN_LOGIN + "=" + login + "&" + LOGIN_PASSWORD + "=" + password,
		dataType: "json",
		success: function(data) {
			j = data;

			if (j.errorMessage != undefined) {
				var reason;
				var result = new ServerError(j.errorMessage, j.errorCode);
				
				if(result.code == 1) {
					reason = CONNEXION_ERROR_BAD_LOGIN_CONTENT;
				} else {
					reason = SERVER_ERROR_CONTENT;
				}
					defineAlert(CONNEXION_ALERT, CONNEXION_ERROR_TITLE,
							 reason, result.message + ", code : " + result.code);
		
				$(CONNEXION_ALERT).show();
		

			} else {
				user = User.fromJSON(j);
	
	
				Cookies.set(CRT_USER_CK, user.toArray());
				
	
				environnement.users[user.id+""] = user; 
				environnement.crtUser = user;
				window.location.href = MAIN_PAGE; 	
		
				return true;
			}

		}, 
		
		error: function (xhr, ajaxOptions, thrownError) {
			result = new ServerError("ServerError", -1);
      		
      		var reason = SERVER_ERROR_CONTENT;
      		
      		defineAlert(CONNEXION_ALERT, CONNEXION_ERROR_TITLE,
							 reason, result.message + ", code : " + result.code);
		
			$(CONNEXION_ALERT).show();
      	
      	}
	
	});

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

User.fromJSON = function(j) {
	
	var result = new User(j.userId, j.login, j.fName, j.lName, j.avatar, false);
	
	if(j.nbFollows != undefined)
		result.nbFollows = j.nbFollows;
	
	if(j.nbFollowers != undefined)
		result.nbFollowers = j.nbFollowers;
	
	if(j.nbComments != undefined)
		result.nbComments = j.nbComments;

	

	
	return result;
}

function addAvatarSize(avatar, size) {
	return avatar + "&size=" + size;
}

User.prototype.modifStatus = function() {
	this.contact = !this.contact;
}

User.prototype.toArray = function() {
	var result = {"userId":this.id, "login":this.login, "fName":this.fName,
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
		var authorId = this.author;
		var request = $.ajax({
			url: SERVER_URL + GETUSER_URL,
			type: 'post',
			data:  GETUSER_ID + "=" + authorId + "&" + GETUSER_YOUR_ID + "=" + environnement.crtUser.id,
			dataType: "json",
			async: false,
			success: function(data) {
				
				if(data.errorMessage == undefined) {
					auth = User.fromJSON(data);
				} else {
					auth = environnement.errorUser;
				}
				
				environnement.users[data.id+""] = auth;				
			}, 
		
			error: function (xhr, ajaxOptions, thrownError) {
				auth = environnement.errorUser;
		  	}
	
		});

	}
	
	
	integratedContent = this.content;
	
	var matchValues;
	
	matchValues = this.content.match(Link.regex);
	for(var j in matchValues) {
		crtMatch = matchValues[j];
		
		crtMatch = crtMatch.replace(/\?/gim, "\\?");
	
		integratedContent = integratedContent.replace(new RegExp(crtMatch, "igm"), Link.toHtml(matchValues[j]));
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



function ServerError(message, code) {
	this.message = message;
	this.code = code;
}




function register(element) {
	var lName = element.lName.value;
	var fName = element.fName.value;
	var mail = element.mail.value;
	var confMail = element.confMail.value;
	var login = element.login.value;
	var pwd = element.password.value;
	var confpwd = element.confPassword.value;
	
	var fail = false;
	
	
	if(mail != confMail) {
		$("#mailConf").addClass("has-error");
		fail = true;
	}
	
	if(pwd != confpwd) {
		$("#passwordConf").addClass("has-error");
		fail = true;
	}
	
	if (fail) {
		alertFail("Echec", "Erreur dans le formulaire d'inscription.");
		return;
	}
	
	
	var request = $.ajax({
		url: SERVER_URL + REGISTER_URL,
		type: 'post',
		data:  REGISTER_LOGIN + "=" + login + 
			"&" + REGISTER_PASSWORD + "=" + pwd + 
			"&" + REGISTER_MAIL + "=" + mail + 
			"&" + REGISTER_FNAME + "=" + fName +
			"&" + REGISTER_LNAME + "=" + lName,
		dataType: "json",
		success: function(data) {
			if(data.errorMessage == undefined) {
				alertSuccess("Succes", "Vous avez été enregisté avec succes.");
				
				connect(login, pwd);				
			} else if (data.errorCode == 1) {
				alertFail("Echec", "Un autre compte dispose du même login/mail. ");
				
				$("#login").addClass("has-error");
				$("#mail").addClass("has-error");
				
			} else {
				alertFail("Echec", "Le server a renvoyé une erreur."
					, new ServerError(data.errorMessage, data.errorCode));
			}			
		}, 
	
		error: function (xhr, ajaxOptions, thrownError) {
	  		alertFail("Erreur", "Erreur de communication avec le server.");
	  	}
	
	});
	
	
}

function alertSuccess(title, content) {
	$("#alert-success .alert-title").first().text(title);
	$("#alert-success .alert-content").first().text(content);
	
	$("#alert-success").show();
}

function alertFail(title, content, serverError) {
	$("#alert-fail .alert-title").first().text(title);
	$("#alert-fail .alert-content").first().text(content);
	
	if(serverError != undefined) {
		$("#alert-fail .alert-content").first().text(content + " (" + serverError.message + " : " + serverError.code + ")");
	} else {
		$("#alert-fail .alert-content").first().text(content);
	}
	
	$("#alert-fail").show();
}

function defineAlert(alertId, alertTitle, alertContent, alertServerError) {
	$(alertId + " " + ALERT_TITLE).first().text(alertTitle);
	
	if(alertServerError != undefined) {
		$(alertId + " " + ALERT_CONTENT).first().text(alertContent + " (" + alertServerError + ")");
	} else {
		$(alertId + " " + ALERT_CONTENT).first().text(alertContent);
	}
}


function hideAlert(element) {
	$(element).css("display", "none");
}


function removeError(elem) {
	$(elem).removeClass("has-error");
}

function setLoading(elem) {
	$(elem).html();
}

function setText(elem) {

}




