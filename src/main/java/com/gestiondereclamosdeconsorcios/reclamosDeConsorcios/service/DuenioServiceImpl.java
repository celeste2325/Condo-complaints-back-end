package com.gestiondereclamosdeconsorcios.reclamosDeConsorcios.service;

import com.gestiondereclamosdeconsorcios.reclamosDeConsorcios.Exceptions.*;
import com.gestiondereclamosdeconsorcios.reclamosDeConsorcios.entity.Duenio;
import com.gestiondereclamosdeconsorcios.reclamosDeConsorcios.entity.Persona;
import com.gestiondereclamosdeconsorcios.reclamosDeConsorcios.entity.Reclamo;
import com.gestiondereclamosdeconsorcios.reclamosDeConsorcios.entity.Unidad;
import com.gestiondereclamosdeconsorcios.reclamosDeConsorcios.entity.dto.DuenioCrearDto;
import com.gestiondereclamosdeconsorcios.reclamosDeConsorcios.entity.dto.DuenioImpresionDto;
import com.gestiondereclamosdeconsorcios.reclamosDeConsorcios.repository.DuenioRepository;
import com.gestiondereclamosdeconsorcios.reclamosDeConsorcios.repository.PersonaRepository;
import com.gestiondereclamosdeconsorcios.reclamosDeConsorcios.repository.UnidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DuenioServiceImpl implements DuenioService {
    @Autowired
    DuenioRepository duenioRepository;
    @Autowired
    UnidadRepository unidadRepository;
    @Autowired
    PersonaRepository personaRepository;


    @Override
    public List<DuenioImpresionDto> getAll() {
        List<DuenioImpresionDto>dueniosDto = new ArrayList<>();
        duenioRepository.findAll().stream().forEach(duenio ->{
                DuenioImpresionDto duenioDto = new DuenioImpresionDto(duenio);
                dueniosDto.add(duenioDto);
        });
        return dueniosDto;
    }

    @Override
    @Transactional
    public void saveDuenio(DuenioCrearDto newDuenio) throws DocumentoNoEncontradoException, DocumentoAsignadoPreviamenteAlAUnidadException {
        //para validar si la persona a la que se quiere asignar como dueño ya existe en la base
        Optional<Persona> personaEncontrada = this.personaRepository.findById(newDuenio.getDocumento());
        if (this.losDatosSonValidos(newDuenio)) {
            if (personaEncontrada.isPresent()) {
                this.duenioRepository.asignarDuenio(newDuenio.getIdentificador(), newDuenio.getDocumento());
            } else
                throw new DocumentoNoEncontradoException("El documento del dueño que desea asignar no corresponde a una persona registrada en el consorcio");
        }
    }

    private boolean losDatosSonValidos(DuenioCrearDto newDuenio) throws DocumentoAsignadoPreviamenteAlAUnidadException {
        //para validar si existe la unidad a la cual quiere asignar el dueño.
        Optional<Unidad> unidadEncontrada = this.unidadRepository.findById(newDuenio.getIdentificador());

        //para validar si el dueño a asignar ya fue asignado a esa misma unidad
        boolean yaFueAsignado = this.duenioRepository.existsByDocumentoAndIdentificador(newDuenio.getDocumento(), unidadEncontrada.get().getIdentificador());
        if (unidadEncontrada.isPresent()) {
            if (!yaFueAsignado) {
                return true;
            } else
                throw new DocumentoAsignadoPreviamenteAlAUnidadException("Ya el duenio fue asignado previamente a la unidad");
        }
        return false;

    }

    @Override
    public List<Duenio> dueniosPorEdificio(Integer codigo) {
        System.out.println(this.duenioRepository.dueniosPorEdificio(codigo));
        return this.duenioRepository.dueniosPorEdificio(codigo);
    }

    @Override
    public List<DuenioImpresionDto> getDuenios(Integer codigoUnidad, Integer idDuenio, String documento) throws NoSeEncontraronDueniosException {
        List<DuenioImpresionDto> dueniosDto = new ArrayList<>();
        List<Duenio> duenios = this.duenioRepository.findAllByIdentificadorOrIdOrDocumento(codigoUnidad,idDuenio,documento);
        if (!duenios.isEmpty()) {
            duenios.stream().forEach(duenio -> {
                DuenioImpresionDto duenioDto = new DuenioImpresionDto(duenio);
                dueniosDto.add(duenioDto);
            });
        }else throw new NoSeEncontraronDueniosException("No se encontraron dueños");
        return dueniosDto;
    }

    @Override
    public void remove(Integer id) {
        duenioRepository.deleteById(id);
    }


}
