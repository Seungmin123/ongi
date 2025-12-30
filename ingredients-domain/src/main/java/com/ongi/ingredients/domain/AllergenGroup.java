package com.ongi.ingredients.domain;

public class AllergenGroup {

	private Long id;

	private String code;

	private String nameKo;

	private AllergenGroup(Long id, String code, String nameKo) {
		this.id = id;
		this.code = code;
		this.nameKo = nameKo;
	}

	public static AllergenGroup create(Long id, String code, String nameKo) {
		return new AllergenGroup(id, code, nameKo);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getNameKo() {
		return nameKo;
	}

	public void setNameKo(String nameKo) {
		this.nameKo = nameKo;
	}
}
