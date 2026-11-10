package br.com.bemdoar.config;

import br.com.bemdoar.entity.CategoriaNecessidade;
import br.com.bemdoar.entity.Instituicao;
import br.com.bemdoar.entity.Necessidade;
import br.com.bemdoar.entity.Usuario;
import br.com.bemdoar.enums.PerfilUsuario;
import br.com.bemdoar.enums.Prioridade;
import br.com.bemdoar.enums.SituacaoNecessidade;
import br.com.bemdoar.repository.CategoriaNecessidadeRepository;
import br.com.bemdoar.repository.InstituicaoRepository;
import br.com.bemdoar.repository.NecessidadeRepository;
import br.com.bemdoar.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cria os dados iniciais na PRIMEIRA vez que o projeto sobe.
 * Se ja existir dado no banco, nao faz nada (pode rodar quantas vezes quiser).
 *
 * RN02 - a conta ADMINISTRADOR e configurada previamente, nunca nasce
 *        do cadastro publico.
 *
 * Esta classe ja esta pronta. NINGUEM precisa alterar este arquivo.
 *
 * ================= CONTAS DE TESTE DO PROJETO =================
 *   ADMIN    admin@bemdoar.com    /  admin123
 *   USUARIO  maria@email.com      /  maria123
 * =============================================================
 */
@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final CategoriaNecessidadeRepository categoriaRepository;
    private final NecessidadeRepository necessidadeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      InstituicaoRepository instituicaoRepository,
                      CategoriaNecessidadeRepository categoriaRepository,
                      NecessidadeRepository necessidadeRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.categoriaRepository = categoriaRepository;
        this.necessidadeRepository = necessidadeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        criarUsuarios();
        criarInstituicao();
        criarCategoriasENecessidades();
    }

    private void criarUsuarios() {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome("Administrador BemDoar");
        admin.setEmail("admin@bemdoar.com");
        admin.setSenha(passwordEncoder.encode("admin123"));
        admin.setTelefone("(11) 90000-0000");
        admin.setDataNascimento(LocalDate.of(1990, 1, 1));
        admin.setPerfil(PerfilUsuario.ADMINISTRADOR);
        admin.setAtivo(true);
        admin.setAceiteTermos(true);
        admin.setDataCadastro(LocalDateTime.now());
        usuarioRepository.save(admin);

        Usuario comum = new Usuario();
        comum.setNome("Maria Doadora");
        comum.setEmail("maria@email.com");
        comum.setSenha(passwordEncoder.encode("maria123"));
        comum.setTelefone("(11) 91111-1111");
        comum.setDataNascimento(LocalDate.of(1998, 5, 20));
        comum.setPerfil(PerfilUsuario.USUARIO);
        comum.setAtivo(true);
        comum.setAceiteTermos(true);
        comum.setDataCadastro(LocalDateTime.now());
        usuarioRepository.save(comum);

        System.out.println(">>> BemDoar: contas criadas - admin@bemdoar.com/admin123 e maria@email.com/maria123");
    }

    private void criarInstituicao() {
        if (instituicaoRepository.count() > 0) {
            return;
        }

        Instituicao i = new Instituicao();
        i.setNome("Instituicao parceira - demonstracao");
        i.setDescricao("Dados ficticios de demonstracao. O nome oficial da parceira esta pendente de confirmacao.");
        i.setMissao("Organizar necessidades e doacoes fisicas para a comunidade.");
        i.setAreaAtuacao("Assistencia social");
        i.setPublicoAtendido("Familias em situacao de vulnerabilidade social");
        i.setCep("00000-000");
        i.setLogradouro("Rua Exemplo");
        i.setNumero("100");
        i.setBairro("Centro");
        i.setCidade("Gama");
        i.setEstado("DF");
        i.setTelefone("(11) 3000-0000");
        i.setEmail("contato@bemdoar.com");
        i.setHorarioAtendimento("Segunda a sexta, 9h as 17h");
        instituicaoRepository.save(i);
    }

    private void criarCategoriasENecessidades() {
        if (categoriaRepository.count() > 0) {
            return;
        }

        CategoriaNecessidade alimentos = novaCategoria("Alimentos", "Alimentos nao pereciveis");
        CategoriaNecessidade roupas = novaCategoria("Roupas e agasalhos", "Vestuario em bom estado");
        CategoriaNecessidade higiene = novaCategoria("Higiene pessoal", "Itens de higiene e limpeza");
        novaCategoria("Material escolar", "Cadernos, lapis e mochilas");

        categoriaRepository.saveAll(List.of(alimentos, roupas, higiene));

        Usuario admin = usuarioRepository.findByEmail("admin@bemdoar.com").orElseThrow();
        novaNecessidade("Cestas basicas", "Cestas basicas completas para 50 familias.",
                alimentos, 50, Prioridade.ALTA, admin);
        novaNecessidade("Cobertores", "Cobertores de solteiro para o inverno.",
                roupas, 100, Prioridade.MEDIA, admin);
        novaNecessidade("Kits de higiene", "Kit com sabonete, pasta de dente e shampoo.",
                higiene, 80, Prioridade.BAIXA, admin);
    }

    private CategoriaNecessidade novaCategoria(String nome, String descricao) {
        CategoriaNecessidade c = new CategoriaNecessidade();
        c.setNome(nome);
        c.setDescricao(descricao);
        c.setAtivo(true);
        return categoriaRepository.save(c);
    }

    private void novaNecessidade(String titulo, String descricao,
                                 CategoriaNecessidade categoria,
                                 int quantidade, Prioridade prioridade, Usuario admin) {
        Necessidade n = new Necessidade();
        n.setTitulo(titulo);
        n.setDescricao(descricao);
        n.setJustificativa("Necessidade de demonstracao para teste do sistema.");
        n.setCriadoPor(admin);
        n.setCategoria(categoria);
        n.setQuantidadeNecessaria(quantidade);
        n.setQuantidadeRecebida(0);
        n.setPrioridade(prioridade);
        n.setSituacao(SituacaoNecessidade.ABERTA);
        n.setDataCriacao(LocalDateTime.now());
        necessidadeRepository.save(n);
    }
}
