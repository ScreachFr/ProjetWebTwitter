function map() {
	var words = this.content.match(/\w+/gim);
	var tf = {};
	
	for (var i in words) {
		if(!tf[words[i]])
			tf[words[i]] = 1;
		else
			tf[words[i]]++;
	}
	
	var v;
	var result;
	
	for(var w in tf) {
		v = tf[w]/words.length;
		result = {"word":w, "tf":v};
		
		emit(this._id, result);
	}
	
}

