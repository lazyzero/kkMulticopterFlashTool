/*
 * Translatrix.java
 *
 * Created on March 11, 2004, 1:57 PM
 */

package lu.tudor.santec.i18n;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.lazyzero.kkMulticopterFlashTool.KKMulticopterFlashTool;

/**
 * The Translatrix class is a kind of translation engine for localized
 * applications. It handles the language resource bundles required by an
 * application and treats them as one. Usage of the Translatrix class is as
 * follows:
 * 
 * 1.) The Translatrix class should be used in a static context, i.e. you don't
 * need to instantiate it. 2.) First thing to do is to call the
 * loadSupportedLocales method. Doing so, enables the Translatrix to provide a
 * default Locale if none was specified and to check specified Locales against
 * supported Locales. 3.) Bundles need to be added only once. Subsequent calls
 * to SetLocale will automatically reload the previously specified Bundles for
 * the given Language.
 * 
 * @author nico.mack@tudor.lu
 * @version 1.0
 */

// ***************************************************************************
// * Class Definition and Members *
// ***************************************************************************

public class Translatrix {
	private static Locale m_Locale = Locale.getDefault();
	private static Hashtable<String,String> m_Translations = new Hashtable<String,String>();
	private static Hashtable<String,String> m_Defaults = new Hashtable<String,String>();
	private static List<String> m_Bundles = new ArrayList<String>();
	private static List<Locale> m_SupportedLocales = new ArrayList<Locale>();
	private static boolean m_DefaultWhenMissing = false;

	private static String c_Null = "NULL";

	private static Pattern c_KeyPattern = Pattern.compile("^language_(\\d)$",
			Pattern.CASE_INSENSITIVE);

	private static Pattern c_LocalePattern = Pattern.compile(
			"^([a-zA-Z]{2})_([a-zA-Z]{2})$", Pattern.CASE_INSENSITIVE);

	private static Pattern c_PlaceHolderPattern = Pattern.compile(
			"(\\$)(\\d+)", Pattern.CASE_INSENSITIVE);

	private static Logger logger = KKMulticopterFlashTool.getLogger();

	// ***************************************************************************
	// * Constructor *
	// ***************************************************************************

	// ---------------------------------------------------------------------------
	/**
	 * Creates a new empty instance of Translatrix. This method will hardly
	 * every be called as the Translatrix is most often used in a static context
	 */
	// ---------------------------------------------------------------------------

	public Translatrix() {
	}

	// ***************************************************************************
	// * Class Primitives *
	// ***************************************************************************

	// ---------------------------------------------------------------------------
	/**
	 * loads the specified bundle for the specified Locale.
	 * 
	 * @param p_Bundle
	 *            specifies the language resource bundle to load
	 * @param p_Locale
	 *            specifies the locale to load the bundle for. Setting p_Locale
	 *            to <CODE>null</CODE> will load the default bundle
	 * @return <CODE>true</CODE> if the specified bundle was successfully
	 *         loaded, <CODE>false</CODE> otherwise
	 */
	// ---------------------------------------------------------------------------

	private static boolean loadBundle(String p_Bundle, Locale p_Locale) {
		ResourceBundle l_Bundle = null;
		Enumeration<String> l_Keys;
		String l_Key, l_Value;
		boolean l_LoadedSuccessfully = false;

		try {
			if (p_Locale == null)
				l_Bundle = ResourceBundle.getBundle(p_Bundle);
			else
				l_Bundle = ResourceBundle.getBundle(p_Bundle, p_Locale);

			if (l_Bundle != null) {
				l_Keys = l_Bundle.getKeys();

				while (l_Keys.hasMoreElements()) {
					l_Key = l_Keys.nextElement();
					l_Value = l_Bundle.getString(l_Key);
					if (l_Value != null && l_Value.trim().length() > 0)
						m_Translations.put(l_Key, l_Value);
				}
				l_LoadedSuccessfully = true;
			}
			loadDefaultBundle(p_Bundle);
		} catch (MissingResourceException p_Exception) {
			logException(
					"MissingResourceException while loading language file",
					p_Exception);
		}

		return l_LoadedSuccessfully;
	}

	// ---------------------------------------------------------------------------

	private static void loadDefaultBundle(String p_Bundle) {
		ResourceBundle l_Bundle = null;
		Enumeration<String> l_Keys;
		String l_Key, l_Value;

		// // check if defaults exist....
		// URL defaults = Translatrix.class.getResource(p_Bundle +
		// ".properties");
		// if (defaults == null) {
		// System.out.println("No default translations found at: " + p_Bundle +
		// ".properties .... trying en_US");
		// }
		//
		// defaults = Translatrix.class.getResource(p_Bundle +
		// "_en_US.properties");

		// l_Bundle = ResourceBundle.getBundle(p_Bundle);

		l_Bundle = ResourceBundle.getBundle(p_Bundle, new Locale("en", "US"));

		if (l_Bundle == null || !l_Bundle.getKeys().hasMoreElements()) {
			logger.info("No default/en_US translations found at: " + p_Bundle
					+ "_en_US.properties .... skipping");
			return;
		}

		if (l_Bundle != null) {
			l_Keys = l_Bundle.getKeys();

			while (l_Keys.hasMoreElements()) {
				l_Key = l_Keys.nextElement();
				l_Value = l_Bundle.getString(l_Key);
				if (l_Value != null && l_Value.trim().length() > 0)
					m_Defaults.put(l_Key, l_Value);
			}
		}
	}

	// ---------------------------------------------------------------------------
	/**
	 * Prints the given message and a stack trace of the specified exception to
	 * Stderr.
	 * 
	 * @param p_Message
	 *            Message to log with exception stack trace
	 * @param p_Exception
	 *            Exception to be logged
	 */
	// ---------------------------------------------------------------------------

	private static void logException(String p_Message, Exception p_Exception) {
		logger.warning(p_Message + ": " + p_Exception);
	}

	// ---------------------------------------------------------------------------
	/**
	 * checks whether the specified Locale is one of the supported Locales
	 * 
	 * @return <CODE>true</CODE> if specified Locale is part of the supported
	 *         Locales, <CODE>false</CODE> otherwise
	 */
	// ---------------------------------------------------------------------------

	private static boolean isSupportedLocale(Locale p_Locale) {
		Locale l_Locale;
		int l_LocaleID;
		boolean l_LocaleIsSupported = false;

		for (l_LocaleID = 0; l_LocaleID < m_SupportedLocales.size(); l_LocaleID++) {
			l_Locale = m_SupportedLocales.get(l_LocaleID);

			if (p_Locale.equals(l_Locale))
				l_LocaleIsSupported = true;
		}

		return l_LocaleIsSupported;
	}

	// ---------------------------------------------------------------------------
	/**
	 * sets current translation locale to specified one
	 * 
	 * @param p_Locale
	 *            New translation Locale
	 */
	// ---------------------------------------------------------------------------

	public static void setLocale(Locale p_Locale) {
		String l_Bundle;
		int l_BundleID;

		if (isSupportedLocale(p_Locale)) {
			m_Locale = p_Locale;
			m_Translations = new Hashtable<String,String>();

			for (l_BundleID = 0; l_BundleID < m_Bundles.size(); l_BundleID++) {
				l_Bundle = m_Bundles.get(l_BundleID);
				loadBundle(l_Bundle, m_Locale);
			}
		} else
			logger.info("Unsupported Locale " + p_Locale.toString()
					+ " specified in call to setLocale()");
	}

	// ---------------------------------------------------------------------------
	/**
	 * sets current translation locale to specified one
	 * 
	 * @param p_Locale
	 *            New translation Locale expressed as string in the xx_YY format
	 */
	// ---------------------------------------------------------------------------

	public static void setLocale(String p_Locale) {
		Matcher l_Matcher = c_LocalePattern.matcher(p_Locale);

		if (l_Matcher.matches())
			setLocale(new Locale(l_Matcher.group(1), l_Matcher.group(2)));
	}

	// ---------------------------------------------------------------------------
	/**
	 * Returns the currently set translation locale
	 * 
	 * @return currently set translation locale
	 */
	// ---------------------------------------------------------------------------

	public static Locale getLocale() {
		return m_Locale;
	}

	// ---------------------------------------------------------------------------
	/**
	 * Returns the default Locale for this application
	 * 
	 * @return the default Locale for this application
	 */
	// ---------------------------------------------------------------------------

	public static Locale getDefaultLocale() {
		if ((m_SupportedLocales != null) && (m_SupportedLocales.size() > 0)) {
			return m_SupportedLocales.get(0);
		}
		return Locale.getDefault();
	}

	// ---------------------------------------------------------------------------
	/**
	 * Adds a new language resource bundle to the Translatrix
	 * 
	 * @param p_Bundle
	 *            specifies the bundle to add
	 */
	// ---------------------------------------------------------------------------

	public static void addBundle(String p_Bundle) {
		if (loadBundle(p_Bundle, m_Locale))
			m_Bundles.add(p_Bundle);
	}

	// ---------------------------------------------------------------------------
	/**
	 * loads the supported locales bundle specified by p_SupportedLocalesBundle.
	 * Supported locales resource file defines the locales supported by this
	 * application.
	 * 
	 * @param p_SupportedLocalesBundle
	 *            specifies the resource bundle containing the supported locales
	 * @param p_LoaderClass
	 *            specifies the class to use class loader of to load supported
	 *            Locales file with
	 */
	// ---------------------------------------------------------------------------

	public static void loadSupportedLocales(String p_SupportedLocalesBundle) {
		ResourceBundle l_Bundle;
		Matcher l_Matcher;
		Enumeration<String> l_Keys;// <String>
		Hashtable<Integer, Locale> l_Locales;// <Integer,Locale>
		String l_Key;
		Integer l_ID;
		int l_Index;

		try {
			l_Bundle = ResourceBundle.getBundle(p_SupportedLocalesBundle,
					Locale.getDefault());
		} catch (Exception p_Exception) {
			logException("Failed to load supportedLocales file", p_Exception);
			return;
		}

		if (l_Bundle != null) {
			l_Locales = new Hashtable<Integer, Locale>();// <Integer,Locale>
			l_Keys = l_Bundle.getKeys();

			// Read supported Locales properties from property file. Properties
			// are not read in an ordered fashion.

			while (l_Keys.hasMoreElements()) {
				l_Key = l_Keys.nextElement();
				l_Matcher = c_KeyPattern.matcher(l_Key);

				if (l_Matcher.matches()) {
					l_ID = new Integer(l_Matcher.group(1));

					l_Matcher = c_LocalePattern.matcher(l_Bundle
							.getString(l_Key));

					if (l_Matcher.matches()) {
						l_Locales.put(l_ID, new Locale(l_Matcher.group(1),
								l_Matcher.group(2)));
					}
				}
			}

			if (l_Locales.size() > 0) {
				m_SupportedLocales = new ArrayList<Locale>();
				for (l_Index = 0; l_Index < l_Locales.size(); l_Index++) {
					l_ID = new Integer(l_Index);
					if (l_Locales.containsKey(l_ID))
						m_SupportedLocales.add(l_Locales.get(l_ID));
				}
			}
		}
	}

	// ---------------------------------------------------------------------------
	/**
	 * Returns a Vector holding all the locales supported by this application
	 * 
	 * @return Vector holding all the locales supported by this application
	 */
	// ---------------------------------------------------------------------------

	public static List<Locale> getSupportedLocales()
	{
		return m_SupportedLocales;
	}

	// ---------------------------------------------------------------------------
	/**
	 * Returns a Vector holding the names of the Bundles added so far
	 * 
	 * @return Vector holding the names of the Bundles loaded so far
	 */
	// ---------------------------------------------------------------------------

	public static List<String> getBundles()
	{
		return m_Bundles;
	}

	// ---------------------------------------------------------------------------
	/**
	 * returns a vector holding the translation keys stored in the Translatrix
	 * 
	 * @return vector holding the translation keys stored in the Translatrix
	 */
	// ---------------------------------------------------------------------------

	public static List<String> getTranslationKeys()
	{
		List<String> l_Keys;
		Enumeration<String> l_TranslationKeys;

		l_Keys = new ArrayList<String>();

		for (l_TranslationKeys = m_Translations.keys(); l_TranslationKeys.hasMoreElements();)
			l_Keys.add(l_TranslationKeys.nextElement());

		return l_Keys;
	}

	// ---------------------------------------------------------------------------
	/**
	 * Checks whether the specified key is known to the Translatrix, i.e. a
	 * translation exists for the given key.
	 * 
	 * @param p_Key
	 *            Specifies the key in the property file referencing the
	 *            translation string
	 * @return <code>true</code> if a translation exists for the specified key,
	 *         <code>false</code> otherwise.
	 */
	// ---------------------------------------------------------------------------

	public static boolean hasTranslationFor(String p_Key) {
		if ((p_Key != null) && (m_Translations != null)) {
			return m_Translations.containsKey(p_Key);
		}
		return false;
	}

	// ---------------------------------------------------------------------------
	/**
	 * Returns the translation String for the given Key in the currently set
	 * translation Locale
	 * 
	 * @param p_Key
	 *            Specifies the key in the property file referencing the
	 *            translation string
	 * @return the translation String for the given Key in the currently set
	 *         translation Locale. If the specified key is <code>null</code> or
	 *         <code>""</code>, the method returns the p_Key itself of "null".
	 */
	// ---------------------------------------------------------------------------

	public static String getTranslationString(String p_Key) {
		String l_Translation = p_Key;

		if (p_Key == null)
			return "null";

		logger.fine("key: " + p_Key);

		if (m_Translations != null) {
			l_Translation = m_Translations.get(p_Key);

			if ((l_Translation == null || l_Translation.trim().equals(""))
					&& isDefaultWhenMissing()) {
				l_Translation = m_Defaults.get(p_Key);
			}

			if (l_Translation == null || l_Translation.trim().equals("")) {
				l_Translation = p_Key;
			}
		}

		logger.fine("\tTranslation: " + l_Translation);

		return l_Translation;
	}

	// ---------------------------------------------------------------------------
	/**
	 * Returns the translation String for the given Key in the currently set
	 * translation Locale
	 * 
	 * @param p_Key
	 *            Specifies the key in the property file referencing the
	 *            translation string
	 * @return the translation String for the given Key in the currently set
	 *         translation Locale. If the specified key is <code>null</code> or
	 *         <code>""</code>, the method returns the p_Key itself of "null".
	 */
	// ---------------------------------------------------------------------------

	public static String getDefaultString(String p_Key) {
		String l_Translation = p_Key;

		if (p_Key == null)
			return "null";

		logger.fine("key: " + p_Key);

		if (m_Defaults != null) {
			l_Translation = m_Defaults.get(p_Key);

			if (l_Translation == null || l_Translation.trim().equals("")) {
				l_Translation = p_Key;
			}
		}

		logger.fine("\tTranslation: " + l_Translation);

		return l_Translation;
	}

	/**
	 * This is a alias for the getTranslationString Method.
	 * 
	 * @see getTranslationString
	 * @param p_Key
	 *            Specifies the key in the property file referencing the
	 *            translation string
	 * @return the translation String for the given Key in the currently set
	 *         translation Locale. If the specified key is <code>null</code> or
	 *         <code>""</code>, the method returns the p_Key itself or "null".
	 */
	public static String _(String p_Key) {
		return getTranslationString(p_Key);
	}

	// ---------------------------------------------------------------------------
	/**
	 * Returns the translation String for the given Key in the currently set
	 * translation Locale. Occurences of $x ($0, $1, $2 ...) placeholders in
	 * translation String will be replaced with the corresponding filler Strings
	 * specified by p_Filler array.
	 * 
	 * @param p_Key
	 *            Specifies the key in the property file referencing the
	 *            translation string
	 * @param p_Filler
	 *            Specifies the Strings to use as replacement for matching
	 *            placeholder occurences
	 * @return the translation String for the given Key in the currently set
	 *         translation Locale
	 */
	// ---------------------------------------------------------------------------

	public static String getTranslationString(String p_Key, String[] p_Filler) {
		String l_Translation = p_Key;
		int l_Index;
		StringBuffer l_Buffer;
		String l_Filler;
		Matcher l_Matcher;

		if (p_Key == null)
			return "null";

		if (m_Translations != null) {
			l_Translation = m_Translations.get(p_Key);
			if (l_Translation != null) {
				if ((p_Filler != null) && (p_Filler.length > 0)) {
					l_Buffer = new StringBuffer();

					l_Matcher = c_PlaceHolderPattern.matcher(l_Translation);

					while (l_Matcher.find()) {
						l_Index = Integer.valueOf(l_Matcher.group(2))
								.intValue();

						if ((l_Index >= 0) && (l_Index < p_Filler.length)) {
							l_Filler = (p_Filler[l_Index] != null) ? p_Filler[l_Index] : c_Null;
							l_Matcher.appendReplacement(l_Buffer, l_Filler);
						}
					}

					l_Matcher.appendTail(l_Buffer);
					l_Translation = l_Buffer.toString();
				}
			} else {
				l_Translation = p_Key;
			}
		}

		return l_Translation;
	}

	// ---------------------------------------------------------------------------
	/**
	 * Returns the translation String for the given Key in the currently set
	 * translation Locale. Occurences of $x ($0, $1, $2 ...) placeholders in
	 * translation String will be replaced with the corresponding filler Strings
	 * specified by p_Filler array.
	 * 
	 * @see getTranslationString
	 * @param p_Key
	 *            Specifies the key in the property file referencing the
	 *            translation string
	 * @param p_Filler
	 *            Specifies the Strings to use as replacement for matching
	 *            placeholder occurences
	 * @return the translation String for the given Key in the currently set
	 *         translation Locale
	 */
	// ---------------------------------------------------------------------------
	public static String _(String p_Key, String[] p_Filler) {
		return getTranslationString(p_Key, p_Filler);
	}

	public static boolean isDefaultWhenMissing() {
		return m_DefaultWhenMissing;
	}

	public static void setDefaultWhenMissing(boolean defaultWhenMissing) {
		m_DefaultWhenMissing = defaultWhenMissing;
	}

	// ***************************************************************************
	// * End Of Class *
	// ***************************************************************************

}
