
// Deactivates all link objects
function deactivateAllLinks()
{
	jQuery("nav .active").each (function (key, value)
	{
		jQuery(value).removeClass("active");
	});
}

// Hides / deactivates all content div's
function deactivateAllContents()
{
	jQuery("#contentWrapper div").each (function (key, value)
	{
		jQuery(value).hide();
	});
}

jQuery(document).ready(function ()
{
	jQuery("#home").show();
	
	// Sitechanger links
	jQuery("nav .siteChanger").click(function ()
	{
		// Deactivate all link objects
		deactivateAllLinks();
		
		// Activate the pressed link object
		jQuery(this).closest("li").addClass("active");
		
		var target = jQuery(this).attr("data-target");
		deactivateAllContents();
		jQuery("#"+target).show();
	});
});