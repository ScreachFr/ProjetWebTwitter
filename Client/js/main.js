const MAIN_PAGE = "index.html";
const LOGIN_PAGE = "login.html";
const REGISTER_PAGE = "register.html";

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

//Cookies
const CRT_USER_CK = "crtUser";
const KEY_CK = "key";
const FOLLOWS_ONLY_CK = "follows-only";


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

const ADD_COMMENT_URL = "comment/add";
const GET_COMMENT_URL = "comment/get";

const FOLLOW_URL = "follow/add";
const UNFOLLOW_URL = "follow/remove";

const REGISTER_LOGIN = "login";
const REGISTER_PASSWORD = "password";
const REGISTER_FNAME = "fname";
const REGISTER_LNAME = "lname";
const REGISTER_MAIL = "email";

const TIME_PATTERN = "YYYY-MM-DD HH:mm:ss";
const MAIN_COMMENT_NB = 10;

const DFT_AVATAR = "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mm"; 


function init() {

	environnement = new Object();
	environnement.users = {};
	environnement.comments = {};
	environnement.commentsQueue = {};
	
	environnement.errorUser = new User(-1, "Error", "Unknown", "User" 
		, "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mm"
		, false);
	
	if(Cookies.get(CRT_USER_CK) !== undefined) {
		environnement.crtUser = User.fromJSON(Cookies.getJSON(CRT_USER_CK));
		environnement.key = Cookies.get(KEY_CK);
		setConnectedUserUI();
		
		var followsOnly;
		
		console.log(Cookies.get(FOLLOWS_ONLY_CK));
		
		if(Cookies.get(FOLLOWS_ONLY_CK) != undefined) {
			if(Cookies.get(FOLLOWS_ONLY_CK) == "true"){
				followsOnly = Cookies.get(FOLLOWS_ONLY_CK);
				$('#follow-only').prop('checked', true);
			} else {
				followsOnly = false;
				$('#follow-only').prop('checked', false);
			}
		} else {
			followsOnly = false;
			$('#follow-only').prop('checked', false);
			Cookies.set(FOLLOWS_ONLY_CK, false);
		}
		
		fillIndexComments(followsOnly);
		
		//setInterval(function, time);
		
		
	} else {
		setNotConnectedUserIU();
		fillIndexComments(false);
		
	}
	
	
}



function autoResize() {
	$.each( $(".auto-resize"), function(index, value) {
		setDefaultSize($(value));
		$(value).removeClass("auto-resize");
	});
}

function setConnectedUserUI() {
	var user = environnement.crtUser;
	
	var login = user.login;
	var name = user.fName + " " + user.lName;
	
	
	
	$(USER_LOGIN_ID).first().text("@" + login);
	$(USER_NAME_ID).first().text(name);
	
	
	(user.nbFollowers != undefined) ? $(USER_FOLLOWERS_ID).first().text(user.nbFollowers+"") : $(USER_FOLLOWERS_ID).first().text("0");
	(user.nbFollows != undefined) ? $(USER_FOLLOWS_ID).first().text(user.nbFollows+"") : $(USER_FOLLOWS_ID).first().text("0");	
	(user.nbMessages != undefined) ? $(USER_COMMENTS_ID).first().text(user.nbMessages+"") : $(USER_COMMENTS_ID).first().text("0");
	
	$(AVATAR_MAIN).each(function() {
		$(this).attr("src", addAvatarSize(user.avatar, DEFAULT_AVATAR_SIZE));
	});

}

function setNotConnectedUserIU() {
	var login = "Paco";
	var name = "Utilisateur non connecté";
	
	
	
	$(USER_LOGIN_ID).first().text("@" + login);
	$(USER_NAME_ID).first().text(name);
	
	
	$(USER_FOLLOWERS_ID).first().text("0");
	$(USER_FOLLOWS_ID).first().text("0");	
	$(USER_COMMENTS_ID).first().text("0");
	
	$(AVATAR_MAIN).each(function() {
		$(this).attr("src", addAvatarSize(DFT_AVATAR, DEFAULT_AVATAR_SIZE));
	});
	
	$(".follows-only-group").hide();
	$(".write-message-content").hide();	
	
	$("#connexion-button").attr("title", "Se connecter");
	

}

function switchFollowsOnlyIndex() {
	
	var fo;	
	var fo_ck = Cookies.get(FOLLOWS_ONLY_CK);
	

	if (fo_ck == "false") {
		Cookies.set(FOLLOWS_ONLY_CK, true);
		fo = true;
	} else {	
		Cookies.set(FOLLOWS_ONLY_CK, false);
		fo = false;
	}
	
	fillIndexComments(fo);
}

function fillIndexComments(followsOnly) {
	alertLoading("Chargement des commentaires.");
	
	
	var iduser;
	if(followsOnly) {
		iduser = environnement.crtUser.id;
	} else {
		iduser = -1;
	}
	
	var now = moment().format(TIME_PATTERN);
	
	var request = $.ajax({
		url: SERVER_URL + GET_COMMENT_URL,
		type: 'post',
		data:  "date=" + now + "&op=b&mresult=" + MAIN_COMMENT_NB + "&iduser=" + iduser,
		dataType: "json",
		success: function(data) {
			hideLoading();				
			if(data.errorMessage == undefined) {
				var comments = SearchResults.fromJSON(data.comments);
				$(COMMENTS_MAIN_CONTAINER).empty();
				
				for (var i in comments) {
					$(COMMENTS_MAIN_CONTAINER).append(comments[i].getHtml());
				}
				autoResize();
			} else {
				alertFail("Echec", "Le server a renvoyé une erreur."
				, new ServerError(data.errorMessage, data.errorCode));
			}
			
			
		}, 
		error: function (xhr, ajaxOptions, thrownError) {
			alertFail("Erreur", "Erreur de communication avec le server.");
	  		hideLoading();
	  	}

	});
	
	
}

function reverseArray(array) {
	var result = {};
	
	for(var i in array) {
		result.push(array[i]);
	}
	
	return result;
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
	if(environnement.crtUser == undefined) {
		window.location.href = LOGIN_PAGE;
	} else {
	
		Cookies.remove(CRT_USER_CK);
		Cookies.remove(KEY_CK);

		var request = $.ajax({
			url: SERVER_URL + DISCONNET_URL,
			type: 'post',
			data: LOGIN_LOGIN + "=" + login,
			dataType: "json",
			async: false,
			success: function(data) {
				window.location.href = LOGIN_PAGE;
			}, 

			error: function (xhr, ajaxOptions, thrownError) {
				window.location.href = LOGIN_PAGE;
			}

		});



		window.location.href = LOGIN_PAGE;	

		return false;
	}
}

function connect(login, password) {
	var user;
	var j;
	
	alertLoading("Connexion");
	
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
		
				hideLoading();
				$(CONNEXION_ALERT).show();

			} else {
				user = User.fromJSON(j);
				
				
				
				Cookies.set(CRT_USER_CK, user.toArray());
				Cookies.set(KEY_CK, j.key);
				
				window.location.href = MAIN_PAGE; 	
				hideLoading();
				return true;
			}

		}, 
		
		error: function (xhr, ajaxOptions, thrownError) {
			result = new ServerError("ServerError", -1);
      		
      		var reason = SERVER_ERROR_CONTENT;
      		
      		defineAlert(CONNEXION_ALERT, CONNEXION_ERROR_TITLE,
							 reason, result.message + ", code : " + result.code);
			hideLoading();
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
	
	
	if(j.contact != undefined)
		result.contact = j.contact;
	
	
	
	if(j.stats != undefined) {
		if(j.stats.follows != undefined)
			result.nbFollows = j.stats.follows;
		if(j.stats.followers != undefined)
			result.nBFollowers = j.stats.followers;
		if(j.stats.comments != undefined)
			result.nbMessages = j.stats.comments;
	}
	
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
	
	
	if(this.nbFollows != undefined || this.nbFollowers != undefined 
		|| this.nbMessages != undefined) {					 
		result['stats'] = {};
		if(this.nbFollows != undefined) 
			result['stats']["follows"] = this.nbFollows;
		if(this.nbFollowers != undefined) 
			result['stats']["followers"] = this.nbFollowers;
		if(this.nbMessages != undefined)
			result['stats']["comments"] = this.nbMessages;
	}
	
	return result;
}

function htmlEntities(str) {
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function Comment(id, author, content, date, score) {
	this.id = id;
	this.author = author;
	this.content = htmlEntities(content);
	this.date = date;
	this.score = 0;
	
	if(score != undefined)
		this.score = score;
	
	
	
	
}

Comment.fromJSON = function(j) {
	var result;
	
	result = new Comment(j._id, j.userid, j.content, j.date, 0);
	
	return result;
}

Comment.prototype.getHtml = function() {
	var result = "";
	var integratedContent;
	var media = "";
	var auth = environnement.users[this.author+""];
	
	if(auth == undefined) {
		var authorId = this.author;
		var data;
		
		if (environnement.crtUser != undefined)
			data = GETUSER_ID + "=" + authorId + "&" + GETUSER_YOUR_ID + "=" + environnement.crtUser.id;
		else
			data = GETUSER_ID + "=" + authorId;
		
		var request = $.ajax({
			url: SERVER_URL + GETUSER_URL,
			type: 'post',
			data:  data,
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
	moment.locale('fr');
	var date = moment(this.date);
	
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

	
	result += '<div class="Message" message-id="' + this.id + '" author-id="' + auth.id + '">\n';
	result += '<img src="' + auth.avatar + '" class="MsgAvatar thumbnail" />\n';
	result += '<div class="MsgMain">\n';
	result += '<p>\n';
	result += '<span class="MsgAuthor">' + auth.fName + " " + auth.lName + '</span>\n';
	result += '<span class="MsgLogin">@' + auth.login + '</span>\n';
	result += '<span class="MsgDate"> - ' + date.fromNow() + '</span>\n';
	if (environnement.crtUser != undefined && auth.id != environnement.crtUser.id) {
		if(auth.contact)
			result += '<button onclick="unfollow(' + auth.id + ')" class="follow-button btn btn-danger" title="Ne plus suivre"><span class="fui-cross"></span></a></button>\n';
		else
			result += '<button onclick="follow(' + auth.id + ')" class="follow-button btn btn-info" title="Suivre"><span class="fui-plus"></span></a></button>\n';
	}
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


/*
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

*/

SearchResults.fromJSON = function(j) {
	var result = {};
	
	
	for(var i in j) {
		result[i] = new Comment(j[i]._id, j[i].userid, j[i].content, j[i].date, 0);
	}
	
	return result;
}

function ServerError(message, code) {
	this.message = message;
	this.code = code;
}




function register(element) {
	alertLoading("Connexion");
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
		hideLoading();
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
			hideLoading();			
		}, 
	
		error: function (xhr, ajaxOptions, thrownError) {
	  		alertFail("Erreur", "Erreur de communication avec le server.");
	  		hideLoading();
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

function alertLoading(content) {
	$("#alert-loading .alert-content").first().text(content);
	
	$("#alert-loading").show();
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

function hideLoading() {
	hideAlert($("#alert-loading"));
}

function removeError(elem) {
	$(elem).removeClass("has-error");
}



function handleCommentSending(form) {
	var content;
	
	content = form.content.value;
	sendComment(Cookies.get(KEY_CK), content);
}

function sendComment(author, content) {
	alertLoading("Envoi du commentaire");
	var request = $.ajax({
			url: SERVER_URL + ADD_COMMENT_URL,
			type: 'post',
			data:  "key=" + author + "&text=" + content,
			dataType: "json",
			async: true,
			success: function(data) {
				hideLoading();				
				if(data.errorMessage == undefined) {
					addCommentToMain(Comment.fromJSON(data.comment));
					$("#write-message-input").val("");
					$("#Comments-v").text(parseInt($("#Comments-v").text())+1);
				} else {
					alertFail("Echec", "Le server a renvoyé une erreur."
					, new ServerError(data.errorMessage, data.errorCode));
				}
				
				
			}, 
			error: function (xhr, ajaxOptions, thrownError) {
				alertFail("Erreur", "Erreur de communication avec le server.");
		  		hideLoading();
		  	}
	
		});
		
}

function addCommentToMain(comment) {
	$(COMMENTS_MAIN_CONTAINER).prepend(comment.getHtml());
}

function follow(idToFollow) {
		alertLoading("Suivation en cours");
		var request = $.ajax({
			url: SERVER_URL + FOLLOW_URL,
			type: 'post',
			data:  "key=" + environnement.key + "&idtofollow=" + idToFollow,
			dataType: "json",
			success: function(data) {
				hideLoading();
				if(data.errorMessage == undefined) {
					environnement.users[idToFollow+""].contact = true;
					environnement.crtUser.nbFollows++;
					Cookies.set(CRT_USER_CK, environnement.crtUser.toArray());
					$("#Follows-v").text(parseInt($("#Follows-v").text())+1);				
					changeFollowButton(idToFollow, true);
				} else {
					alertFail("Echec", "Le server a renvoyé une erreur."
					, new ServerError(data.errorMessage, data.errorCode));
				}
				
				
			}, 
			error: function (xhr, ajaxOptions, thrownError) {
				alertFail("Erreur", "Erreur de communication avec le server.");
		  		hideLoading();
		  	}
	
		});
}

function unfollow(idToUnfollow) {
		alertLoading("Dessuivation en cours");
		var request = $.ajax({
			url: SERVER_URL + UNFOLLOW_URL,
			type: 'post',
			data:  "key=" + environnement.key + "&idtounfollow=" + idToUnfollow,
			dataType: "json",
			success: function(data) {
				hideLoading();				
				if(data.errorMessage == undefined) {
					environnement.users[idToUnfollow+""].contact = false;
					environnement.crtUser.nbFollows--;
					Cookies.set(CRT_USER_CK, environnement.crtUser.toArray());
					$("#Follows-v").text(parseInt($("#Follows-v").text())-1);
					changeFollowButton(idToUnfollow, false);
				} else {
					alertFail("Echec", "Le server a renvoyé une erreur."
					, new ServerError(data.errorMessage, data.errorCode));
				}
				
				hideLoading();
			}, 
			error: function (xhr, ajaxOptions, thrownError) {
				alertFail("Erreur", "Erreur de communication avec le server.");
		  		hideLoading();
		  	}
	
		});
}

function changeFollowButton(id, follow) {
	//var messages = $(".Message[author-id=" + id + "] .follow-button");
	
	
	$(".Message[author-id=" + id + "] .follow-button").each(function (index, value) {
		if(follow) {
			value.setAttribute("class", "follow-button btn btn-danger");
			value.setAttribute("onclick", "unfollow(" + id + ")");
			value.innerHTML = "<span class='fui-cross'></span>";
		} else {
			value.setAttribute("class", "follow-button btn btn-info");
			value.setAttribute("onclick", "follow(" + id + ")");
			value.innerHTML = "<span class='fui-plus'></span>";
		}
	});
		
}




function fetchNewComments() {
	
}









