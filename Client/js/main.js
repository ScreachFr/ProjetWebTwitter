//Paœges
const MAIN_PAGE = "index.html";
const LOGIN_PAGE = "login.html";
const REGISTER_PAGE = "register.html";

const SERVER_URL = " http://li328.lip6.fr:8280/gr3_dupas_gaspar/";
//const SERVER_URL = " http://agaspard.freeboxos.fr:8080/gr3_dupas_gaspar/";

//Alert const
const ALERT_TITLE = ".alert-title";
const ALERT_CONTENT = ".alert-content";

const CONNEXION_ALERT = "#alert-connexion";

//Errors
const CONNEXION_ERROR_TITLE = "Echec de la connexion";
const CONNEXION_ERROR_BAD_LOGIN_CONTENT = "Mauvais login et/ou mot de passe.";
const SERVER_ERROR_CONTENT = "Erreur server.";

//Connected user's elements id
const USER_LOGIN_ID = "#User-login";
const USER_NAME_ID = "#User-name";
const USER_FOLLOWS_ID = "#Follows-v";
const USER_FOLLOWERS_ID = "#Followers-v";
const USER_COMMENTS_ID = "#Comments-v";

//Main elements
const COMMENTS_MAIN_CONTAINER = "#Messages";

const AVATAR_MAIN = ".avatar-main";

//Cookies
const CRT_USER_CK = "crtUser";
const KEY_CK = "key";
const FOLLOWS_ONLY_CK = "follows-only";


//Server args
const CONNECTION_TEST_URL = "connection/test";

//Server params and urls

//Login related
const LOGIN_URL = "user/login";
const LOGIN_LOGIN = "login";
const LOGIN_PASSWORD = "password";

const DISCONNET_URL = "user/logout";

//User related
const GETUSER_URL = "user/get";
const GETUSER_ID = "id";
const GETUSER_YOUR_ID = "yourid";

const REGISTER_URL = "user/create";

//Comment related
const ADD_COMMENT_URL = "comment/add";
const GET_COMMENT_URL = "comment/get";
const SEARCH_URL = "comment/search";

//Follow related
const FOLLOW_URL = "follow/add";
const UNFOLLOW_URL = "follow/remove";

//Register related
const REGISTER_LOGIN = "login";
const REGISTER_PASSWORD = "password";
const REGISTER_FNAME = "fname";
const REGISTER_LNAME = "lname";
const REGISTER_MAIL = "email";


//Default values
const MAIN_COMMENT_NB = 10;
const DFT_AVATAR = "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mm"; 
const DEFAULT_AVATAR_SIZE = 150;

const TIME_PATTERN = "YYYY-MM-DD HH:mm:ss";


function init() {
	//inits env
	environnement = new Object();
	environnement.users = {};
	environnement.comments = {};
	environnement.commentsQueue = {};
	
	environnement.errorUser = new User(-1, "Error", "Unknown", "User" 
									, DFT_AVATAR, false);
	
	setDefaultAvatar();
	
	//hide nav form if user is not in index
	var tmp = window.location.href.split(/(\/)/gim);
	var crtFile = tmp[tmp.length-1];

	if (crtFile != "index.html") {
		hideSeachForm();
	}
	
	//User is not connected
	if(Cookies.get(CRT_USER_CK) !== undefined) {
		environnement.crtUser = User.fromJSON(Cookies.getJSON(CRT_USER_CK));
		environnement.key = Cookies.get(KEY_CK);
		setConnectedUserUI();
		
		var followsOnly;
		
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
	//User is not connected	
	} else {
		if (crtFile == "index.html") {
			setNotConnectedUserIU();
			fillIndexComments(false);
		}
	}
	
	
}

//******************
//*  Utils         *
//******************

function setDefaultAvatar() {
	$(AVATAR_MAIN).each(function() {
		$(this).attr("src", addAvatarSize(DFT_AVATAR, DEFAULT_AVATAR_SIZE));
	});
}


//Resize images from comments
function autoResize() {
	$.each( $(".auto-resize"), function(index, value) {
		setDefaultSize($(value));
		$(value).removeClass("auto-resize");
	});
}

function setConnectedUserUI() {
	var user = environnement.crtUser;
	
	console.log(user);
	
	var login = user.login;
	var name = user.fName + " " + user.lName;
	
	
	//Sets user infos
	$(USER_LOGIN_ID).first().text("@" + login);
	$(USER_NAME_ID).first().text(name);
	
	//Sets user stats
	(user.nbFollowers != undefined) ? $(USER_FOLLOWERS_ID).first().text(user.nbFollowers+"") : $(USER_FOLLOWERS_ID).first().text("0");
	(user.nbFollows != undefined) ? $(USER_FOLLOWS_ID).first().text(user.nbFollows+"") : $(USER_FOLLOWS_ID).first().text("0");	
	(user.nbMessages != undefined) ? $(USER_COMMENTS_ID).first().text(user.nbMessages+"") : $(USER_COMMENTS_ID).first().text("0");
	
	//Set all avatars
	$(AVATAR_MAIN).each(function() {
		$(this).attr("src", addAvatarSize(user.avatar, DEFAULT_AVATAR_SIZE));
	});

}


function setNotConnectedUserIU() {
	var login = "Paco";
	var name = "Utilisateur non connecté";
	
	
	//Set default infos
	$(USER_LOGIN_ID).first().text("@" + login);
	$(USER_NAME_ID).first().text(name);
	
	//Set stats to 0
	$(USER_FOLLOWERS_ID).first().text("0");
	$(USER_FOLLOWS_ID).first().text("0");	
	$(USER_COMMENTS_ID).first().text("0");
	
	//Set default avatar
	$(AVATAR_MAIN).each(function() {
		$(this).attr("src", addAvatarSize(DFT_AVATAR, DEFAULT_AVATAR_SIZE));
	});
	
	//Hide useless stuff
	$(".follows-only-group").hide();
	$(".write-message-content").hide();	
	
	$("#connexion-button").attr("title", "Se connecter");
	

}


//Switch comment to "follows only"
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

//Add size parameter to gravatar url.
//You shouldn't be using this twice.
function addAvatarSize(avatar, size) {
	return avatar + "&size=" + size;
}

//Make a string html stafe.
function htmlEntities(str) {
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}



function reverseArray(array) {
	var result = {};
	
	for(var i in array) {
		result.push(array[i]);
	}
	
	return result;
}

//Redirect to index.html
function gotoIndex() {
	window.location.href = MAIN_PAGE;
}

//******************
//*  Login         *
//******************



//Try to connect an user
function connexion(form) {
	var login = form.login.value;
	var password = form.password.value;
	
	
	var result = connect(login, password);
	
}

//Disconnect an user
//If nobody is connected, this will redirect you to the login page
function disconnect() {
	if(environnement.crtUser == undefined) {
		window.location.href = LOGIN_PAGE;
	} else {
	
		//Remove cookies
		Cookies.remove(CRT_USER_CK);
		Cookies.remove(KEY_CK);
		
		//Send logout request
		var request = $.ajax({
			url: SERVER_URL + DISCONNET_URL,
			type: 'post',
			data: LOGIN_LOGIN + "=" + login,
			dataType: "json",
			async: false,
			success: function(data) {
				//Redirect to login
				window.location.href = LOGIN_PAGE;
			}, 

			error: function (xhr, ajaxOptions, thrownError) {
				//Redirect to login
				window.location.href = LOGIN_PAGE;
			}

		});


		//Redirect to login
		window.location.href = LOGIN_PAGE;	

		return false;
	}
}

//Try to connect someone.
//If it works, this will redirect you to the index.
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
				
				//Set connected user cookies
				Cookies.set(CRT_USER_CK, user.toArray());
				Cookies.set(KEY_CK, j.key);
				
				
				gotoIndex();
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


//******************
//*  Register      *
//******************




//Try to register someone.
function register(element) {
	alertLoading("Connexion");
	//Get parameters from form.
	var lName = element.lName.value;
	var fName = element.fName.value;
	var mail = element.mail.value;
	var confMail = element.confMail.value;
	var login = element.login.value;
	var pwd = element.password.value;
	var confpwd = element.confPassword.value;
	
	var fail = false;
	
	//Check if both entered emails are equals.
	if(mail != confMail) {
		$("#mailConf").addClass("has-error");
		fail = true;
	}
	
	//Same as email but for passwords.
	if(pwd != confpwd) {
		$("#passwordConf").addClass("has-error");
		fail = true;
	}
	
	if (fail) {
		alertFail("Echec", "Erreur dans le formulaire d'inscription.");
		hideLoading();
		return;
	}
	
	//If the it goes to this point, it means that the information entered in 
	//form a correct, at least correct enough to be send to the server.
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

//******************
//*  Alerts        *
//******************



//Basique server error use in error alerts.
function ServerError(message, code) {
	this.message = message;
	this.code = code;
}


//Displays success alert.
function alertSuccess(title, content) {
	$("#alert-success .alert-title").first().text(title);
	$("#alert-success .alert-content").first().text(content);
	
	$("#alert-success").show();
}

//Displays fail alert.
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

//Display login
function alertLoading(content) {
	$("#alert-loading .alert-content").first().text(content);
	
	$("#alert-loading").show();
}

//define an alert
//you shouldn't be using this. Use alertSuccess/alertFail/alertLoading instead.
function defineAlert(alertId, alertTitle, alertContent, alertServerError) {
	$(alertId + " " + ALERT_TITLE).first().text(alertTitle);
	
	if(alertServerError != undefined) {
		$(alertId + " " + ALERT_CONTENT).first().text(alertContent + " (" + alertServerError + ")");
	} else {
		$(alertId + " " + ALERT_CONTENT).first().text(alertContent);
	}
}

//Hide an element (put display to "none").
function hideAlert(element) {
	$(element).css("display", "none");
}

function hideLoading() {
	hideAlert($("#alert-loading"));
}

//Remove error class to an element. 
//This should be used for inputs.
function removeError(elem) {
	$(elem).removeClass("has-error");
}




//******************
//*  Follow        *
//******************



//Try to make the connected user follow someone.
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

//Try to make the connected user follow someone.
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

//Switch follow button to follow<->unfollow in every comments of someone.
function changeFollowButton(id, follow) {
	
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






//******************
//*    User        *
//******************
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

//Create an User from JSON
User.fromJSON = function(j) {
	
	var result = new User(j.userId, j.login, j.fName, j.lName, j.avatar, false);
	
	
	if(j.contact != undefined)
		result.contact = j.contact;
	
	
	
	if(j.stats != undefined) {
		if(j.stats.follows != undefined)
			result.nbFollows = j.stats.follows;
		if(j.stats.followers != undefined)
			result.nbFollowers = j.stats.followers;
		if(j.stats.comments != undefined)
			result.nbMessages = j.stats.comments;
	}
	
	return result;
}


//Switch user following status
User.prototype.modifStatus = function() {
	this.contact = !this.contact;
}

//Convert an User to array.
//Usefull to store someone in a cookie.
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




//******************
//*  SearchResults *
//******************


function SearchResults(results, query, contactOnly, author, date) {
	this.results = results;
	this.contactOnly = contactOnly;
	this.author = author;
	
	this.date = new Date();
	if (date != undefined)
		this.date = date;
	
	this.query = query;
	
}


//True version because it use a real search result instead of a bunch of comments.
SearchResults.fromJSON_trueVersion = function(j) {
	var ret;
	
	var comments = {};
	var query = j.query;
	
	var results = j.results;
	var crtComment;
	
	
	for (var i in results) {
		crtComment = Comment.fromJSON(results[i].comment);
		crtComment.score = results[i].score;
		comments[i] = crtComment;
	}
	
	console.log(query);
	console.log(comments);
	
	ret = new SearchResults(comments, query, false, 0);

	return ret;
}

SearchResults.prototype.toHTML = function() {
	var result = "";
	
	for(var i in this.results) {
		result += this.results[i].getHtml();
	}
	
	return result;
}

SearchResults.fromJSON = function(j) {
	var result = {};
	
	
	for(var i in j) {
		result[i] = new Comment(j[i]._id, j[i].userid, j[i].content, j[i].date, 0);
	}
	
	return result;
}



//Search something
function search(searchForm) {
	var query = searchForm.query.value;
	
	//Don't do anything if there is no query in input.
	if (query != "") {
		//Switch view to search mode.
		switchToSearch(query);
		alertLoading("Recherche");
		$(COMMENTS_MAIN_CONTAINER).empty();
		
		//Try to search stuff
		var request = $.ajax({
			url: SERVER_URL + SEARCH_URL,
			type: 'post',
			data:  "query=" + query,
			dataType: "json",
			success: function(data) {
				if(data.errorMessage == undefined) {
					var result = SearchResults.fromJSON_trueVersion(data);
					
					console.log(result);
					
					$(COMMENTS_MAIN_CONTAINER).prepend(result.toHTML());
				} else {
					alertFail("Echec", "Le server a renvoyé une erreur."
					, new ServerError(data.errorMessage, data.errorCode));
				}
				
				hideLoading();
			}, 
			error: function (xhr, ajaxOptions, thrownError) {
				console.log(xhr);
				alertFail("Erreur", "Erreur de communication avec le server.");
		  		hideLoading();
		  	}
	
		});
	}
	
	
	
}

//Switch to search mode.
function switchToSearch(query) {
	$(".write-message-content").hide();	
	$(".search-element").show();
	$(".search-query").text(query);
	document.title = 'Recherche : "' + query + '" | Gazouilleur';
}



//Hide search elements
function hideSeachForm() {
	$(".navbar-form").hide();
}




//******************
//*  Comments      *
//******************



function Comment(id, author, content, date, score) {
	this.id = id;
	this.author = author;
	this.content = htmlEntities(content);
	this.date = date;
	this.score = 0;
	
	if(score != undefined)
		this.score = score;
	
	
	
	
}

//Create a comment from a JSON object.
Comment.fromJSON = function(j) {
	var result;
	
	result = new Comment(j._id, j.userid, j.content, j.date, 0);
	
	return result;
}

//Convert the comment to html. Will also integrate stuffs.
Comment.prototype.getHtml = function() {
	var result = "";
	var integratedContent;
	var media = "";
	var auth = environnement.users[this.author+""];
	
	//If the author is not in environnement, ask the server who is he.
	if(auth == undefined) {
		var authorId = this.author;
		var data;
		
		//Don't ask the server if the user is following the comment author 
		//if nobody is connected.
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
	
	//Make the date sweeter.
	moment.locale('fr');
	var date = moment(this.date);
	
	integratedContent = this.content;
	
	var matchValues;
	
	//Make links sweeter.
	matchValues = this.content.match(Link.regex);
	for(var j in matchValues) {
		crtMatch = matchValues[j];
		
		crtMatch = crtMatch.replace(/\?/gim, "\\?");
	
		integratedContent = integratedContent.replace(new RegExp(crtMatch, "igm"), Link.toHtml(matchValues[j]));
	}
			
	//Integrate stuffs
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

	//Finally generate comment
	result += '<div class="Message" message-id="' + this.id + '" author-id="' + auth.id + '">\n';
	result += '<img src="' + auth.avatar + '" class="MsgAvatar thumbnail" />\n';
	result += '<div class="MsgMain">\n';
	result += '<p>\n';
	result += '<span class="MsgAuthor">' + auth.fName + " " + auth.lName + '</span>\n';
	result += '<span class="MsgLogin">@' + auth.login + '</span>\n';
	result += '<span class="MsgDate"> - ' + date.fromNow() + '</span>\n';
	//Disply button depending user's following status.
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


//Fill main message container with comments
function fillIndexComments(followsOnly) {
	alertLoading("Chargement des commentaires.");
	
	
	var iduser;
	if(followsOnly) {
		iduser = environnement.crtUser.id;
	} else {
		iduser = -1;
	}
	
	var now = moment().format(TIME_PATTERN);
	
	//Request comments
	var request = $.ajax({
		url: SERVER_URL + GET_COMMENT_URL,
		type: 'post',
		data:  "date=" + now + "&op=b&mresult=" + MAIN_COMMENT_NB + "&iduser=" + iduser,
		dataType: "json",
		success: function(data) {
			hideLoading();	
			//No error			
			if(data.errorMessage == undefined) {
				var comments = SearchResults.fromJSON(data.comments);
				$(COMMENTS_MAIN_CONTAINER).empty();
				
				for (var i in comments) {
					environnement.comments[comments[i].id +""] = 1;
					$(COMMENTS_MAIN_CONTAINER).append(comments[i].getHtml());
				}
				
				autoResize();
			//Error
			} else {
				alertFail("Echec", "Le server a renvoyé une erreur."
				, new ServerError(data.errorMessage, data.errorCode));
			}
			
			
		}, 
		//Fatal error
		error: function (xhr, ajaxOptions, thrownError) {
			alertFail("Erreur", "Erreur de communication avec le server.");
	  		hideLoading();
	  	}

	});
	
	
}


function handleCommentSending(form) {
	var content;
	
	content = form.content.value;
	sendComment(Cookies.get(KEY_CK), content);
}

//Send a comment to the server
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
					environnement.crtUser.nbMessages++;
					Cookies.set(CRT_USER_CK, environnement.crtUser.toArray());
					
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

//Add a comment at the beginning of comment container (works like a prepend).

function addCommentToMain(comment) {
	environnement.comments[comment.id + ""] = 1;
	$(COMMENTS_MAIN_CONTAINER).prepend(comment.getHtml());
	autoResize();
}





