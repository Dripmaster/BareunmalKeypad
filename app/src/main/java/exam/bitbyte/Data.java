package exam.bitbyte;


public class Data
{
	private int title;
	private String description;
	private int image;
    private boolean checked;
	public Data()
	{
		super();
		
	}

	public Data(int title, String description, int image)
	{
		super();
		this.title = title;
		this.description = description;
		//this.image = image;
		if(R.string.title_3==title)
		{
			
		}
		else
		{
			this.image=image;
		}
		if(image==R.drawable.off)
		{
			checked=false;
		}
		else
			checked=true;
	}
	public boolean getchecked()
	{
		return this.checked;
	}
	public void setchceck()
	{
		this.checked=!this.checked;
	}
	public int getTitle()
	{
		return title;
	}
	public void setTitle(int title)
	{
		this.title = title;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public int getImage()
	{
		return image;
	}

	public void setImage(int image)
	{
		this.image = image;
	}
}
