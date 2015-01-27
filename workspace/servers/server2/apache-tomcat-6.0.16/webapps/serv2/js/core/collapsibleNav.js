/*
Collapsible nav 0.1.0 - plugin for collapsing navigation and remembering the closed/open state
http://scottdarby.com/

Copyright (c) 2009 Scott Darby

Requires: jQuery 1.3
		  jquery.cookie plugin by Klaus Hartl

Licensed under the GPL license:
http://www.gnu.org/licenses/gpl.html
*/

jQuery.fn.collapsibleNav = function(options) {

	var defaults = {
		hideByDefault: 'categories,archives', //ids of elements you want hidden by default
		clickController: 'h2', //your heading elements
		slideSpeed: 150 //speed of animation
	};

	//initial variables
	var opts = jQuery.extend(defaults, options),
		$list = jQuery(this),
		$heading = $list.find(opts.clickController),
		$listItems = $list.children(),
		closedItems;
	
	//function that is called when slidable is clicked
	function updateArray(e) {
		//get id of element for adding to array
		var eId = jQuery(e).attr('id');
		
		//look for element in cookie array
		var arrPos = jQuery.inArray(eId, closedItems);

		if (arrPos == -1) {
			//element not in array, add element
			closedItems.push(eId);
		} else {
			//element in array, remove element
			closedItems.splice(arrPos, 1);
		}
		//update cookie
		jQuery.cookie('closedItems', closedItems, { path: '/', expires: 365 });
	}
	
	//remember closed/open state of nav items
	if (jQuery.cookie) {
		
		/*
		  if no cookie called "closedItems" exists, then create one with
		  elements you want hidden by default.
		  '1' is given as first value to prevent cookie from being 
		  deleted if it contains no ids
		*/
		
		if (!jQuery.cookie('closedItems')) {
			jQuery.cookie('closedItems', '1,'+opts.hideByDefault, { path: '/', expires: 365 });
		}

		//if cookie exists
		if (jQuery.cookie('closedItems')) {
			
			//split cookie into array
			closedItems = jQuery.cookie('closedItems').split(',');
			//iterate through array and apply closed class to each element within it
			for (var i = 0; i < closedItems.length; ++i) {
				jQuery('#' + closedItems[i]).addClass('closed');
			}
			//hide content underneath each element that has a class of closed
			jQuery('.closed').children('ul').hide();
		}
	}
	
	$heading.append('<span class="toggleIcon"> </span>');
	
	$heading.click(function(){

		var $target = jQuery(this),
			$parent = $target.parent();

		//toggle closed class
		$parent.toggleClass('closed');

		//slidey stuff
		$target.next().slideToggle(opts.slideSpeed);

		//update cookie with current li
		updateArray($parent);

		return false;

	});
	
	return this;
	
};