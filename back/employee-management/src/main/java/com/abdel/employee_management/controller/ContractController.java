package com.abdel.employee_management.controller;

import com.abdel.employee_management.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @GetMapping("/{id}/contract")
    public ResponseEntity<byte[]> generateContract(@PathVariable Long id) {
        byte[] pdf = contractService.generateContract(id);

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("contract-" + id + ".pdf")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(disposition);
        headers.setContentType(MediaType.APPLICATION_PDF);

        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}