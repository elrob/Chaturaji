function popup(mylink)
{
    if (! window.focus)return true;
    var href;
    if (typeof(mylink) == 'string')
	href=mylink;
    else
	href=mylink.href;
    window.open(href, 'Imperial Chaturaji Rules', 'width=400,scrollbars=yes');
  //  window.open(href, 'Imperial Chaturaji Rules', 'width=400,height=200,scrollbars=yes');
    return false;
}
