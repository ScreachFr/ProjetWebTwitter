function map() {
	var words = this.content.match(/\w+/gim);
	var df = {};


	for(var i in words) {
		if (!df[words[i]])
			df[words[i]] = 1;
	}
	
	for(var w in df) {
		emit(w, 1);
	}
}
